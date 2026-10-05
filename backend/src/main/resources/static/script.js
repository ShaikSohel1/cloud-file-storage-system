// ==========================================================================
// CloudStorage - Supabase Auth & Multi-User Data Isolation Client
// ==========================================================================

const API_BASE = window.location.origin;

// Public Supabase Configuration (DO NOT put service_role or DB passwords here)
const SUPABASE_URL = "https://sqatowxytdyauwqlmkcx.supabase.co";
const SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNxYXRvd3h5dGR5YXV3cWxta2N4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTEwOTkwMzgsImV4cCI6MjEwNjY3NTAzOH0.qEPuvdQrndwlVQY3z7g21HvKbzAvaKRmdMI-wJqUCzg";

// Initialize the official Supabase JavaScript Client safely (avoiding global variable name collision)
const supabaseClient = (typeof window.supabase !== "undefined" && typeof window.supabase.createClient === "function")
    ? window.supabase.createClient(SUPABASE_URL, SUPABASE_ANON_KEY)
    : null;

// ==========================================================================
// Centralized Authentication State
// ==========================================================================
const authService = {
    user: null,
    session: null,
    loading: true,

    async init() {
        const overlay = document.getElementById("authLoadingOverlay");
        const dismissOverlay = () => {
            if (overlay) {
                overlay.style.opacity = "0";
                overlay.style.transition = "opacity 0.2s ease";
                setTimeout(() => { overlay.style.display = "none"; }, 200);
            }
        };

        if (!supabaseClient) {
            console.error("Supabase client failed to initialize. Check CDN script tag.");
            dismissOverlay();
            this.handleAuthRequired();
            authUI.showAlert("login", "Supabase client not loaded. Please check your internet connection.");
            return;
        }

        try {
            // 1. Check existing Supabase session
            const { data: { session }, error } = await supabaseClient.auth.getSession();
            if (error) {
                console.warn("Session check error:", error.message);
            }

            if (session && session.user) {
                this.session = session;
                this.user = session.user;
                this.handleAuthSuccess(session);
            } else {
                this.handleAuthRequired();
            }

            // 2. Subscribe to auth state transitions
            supabaseClient.auth.onAuthStateChange(async (event, newSession) => {
                if (event === "SIGNED_IN" || event === "TOKEN_REFRESHED") {
                    if (newSession) {
                        this.session = newSession;
                        this.user = newSession.user;
                        this.handleAuthSuccess(newSession);
                    }
                } else if (event === "SIGNED_OUT") {
                    this.session = null;
                    this.user = null;
                    this.handleAuthRequired();
                }
            });
        } catch (err) {
            console.error("Initialization error:", err);
            this.handleAuthRequired();
        } finally {
            this.loading = false;
            dismissOverlay();
        }
    },

    async login(email, password) {
        if (!supabaseClient) throw "Supabase client is not loaded";
        const { data, error } = await supabaseClient.auth.signInWithPassword({
            email: email.trim(),
            password: password
        });

        if (error) {
            throw this.formatAuthError(error);
        }

        this.session = data.session;
        this.user = data.user;
        return data;
    },

    async signup(email, password, fullName) {
        if (!supabaseClient) throw "Supabase client is not loaded";
        const { data, error } = await supabaseClient.auth.signUp({
            email: email.trim(),
            password: password,
            options: {
                data: {
                    full_name: fullName.trim()
                }
            }
        });

        if (error) {
            throw this.formatAuthError(error);
        }

        return data;
    },

    async logout() {
        try {
            if (supabaseClient) {
                await supabaseClient.auth.signOut();
            }
        } catch (e) {
            console.error("Sign out error:", e);
        } finally {
            this.session = null;
            this.user = null;
            this.clearUserData();
            this.handleAuthRequired();
            showToast("You have been signed out.", "info");
        }
    },

    async resetPassword(email) {
        if (!supabaseClient) throw "Supabase client is not loaded";
        const { data, error } = await supabaseClient.auth.resetPasswordForEmail(email.trim(), {
            redirectTo: window.location.origin
        });

        if (error) {
            throw this.formatAuthError(error);
        }

        return data;
    },

    getAccessToken() {
        return this.session ? this.session.access_token : null;
    },

    handleAuthSuccess(session) {
        // Hide Auth Cards, Show Main Application
        document.getElementById("authContainer").style.display = "none";
        document.getElementById("appLayout").style.display = "flex";

        // Update User UI elements
        const user = session.user;
        const meta = user.user_metadata || {};
        const fullName = meta.full_name || meta.name || user.email.split("@")[0];
        const email = user.email || "";
        const initial = fullName ? fullName[0].toUpperCase() : "U";

        document.getElementById("userAvatar").innerText = initial;
        document.getElementById("userNameLabel").innerText = fullName;
        document.getElementById("userEmailLabel").innerText = email;
        document.getElementById("welcomeGreeting").innerText = `Welcome back, ${fullName}`;
        document.getElementById("statAuthEmail").innerText = email;

        // Fetch user's isolated private files and storage statistics
        loadFiles();
        loadStats();
    },

    handleAuthRequired() {
        // Hide Main Application, Show Auth Cards
        document.getElementById("appLayout").style.display = "none";
        document.getElementById("authContainer").style.display = "flex";
        authUI.showView("login");
    },

    clearUserData() {
        allFiles = [];
        renderFiles([]);
        document.getElementById("statTotalFiles").innerText = "0";
        document.getElementById("statStorageUsed").innerText = "0 KB";
        document.getElementById("storagePercentText").innerText = "0%";
        document.getElementById("storageProgressBar").style.width = "0%";
    },

    formatAuthError(err) {
        const msg = (err.message || "").toLowerCase();
        if (msg.includes("invalid login credentials") || msg.includes("invalid_grant")) {
            return "Incorrect email or password.";
        }
        if (msg.includes("user already registered") || msg.includes("already exists")) {
            return "An account with this email already exists.";
        }
        if (msg.includes("password should be at least")) {
            return "Password must be at least 6 characters long.";
        }
        if (msg.includes("rate limit") || msg.includes("too many requests")) {
            return "Too many attempts. Please wait a moment and try again.";
        }
        return err.message || "Authentication error. Please try again.";
    }
};

// ==========================================================================
// Auth UI Controls (Forms, Views, Validation)
// ==========================================================================
const authUI = {
    currentView: "login",

    showView(viewName) {
        this.currentView = viewName;
        ["login", "signup", "forgot"].forEach(v => {
            const el = document.getElementById(`${v}View`);
            if (el) el.style.display = v === viewName ? "block" : "none";
            this.clearAlert(v);
        });
    },

    showAlert(view, message, type = "error") {
        const alertEl = document.getElementById(`${view}Alert`);
        if (alertEl) {
            alertEl.className = `auth-alert ${type}`;
            alertEl.innerText = message;
            alertEl.style.display = "block";
        }
    },

    clearAlert(view) {
        const alertEl = document.getElementById(`${view}Alert`);
        if (alertEl) {
            alertEl.style.display = "none";
            alertEl.innerText = "";
        }
    },

    togglePasswordVisibility(inputId, btn) {
        const input = document.getElementById(inputId);
        if (!input) return;
        if (input.type === "password") {
            input.type = "text";
            btn.innerHTML = `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9.88 9.88a3 3 0 1 0 4.24 4.24"/><path d="M10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68"/><path d="M6.61 6.61A13.526 13.526 0 0 0 2 12s3 7 10 7a9.74 9.74 0 0 0 5.39-1.61"/><line x1="2" x2="22" y1="2" y2="22"/></svg>`;
        } else {
            input.type = "password";
            btn.innerHTML = `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"/><circle cx="12" cy="12" r="3"/></svg>`;
        }
    },

    async handleLogin(e) {
        e.preventDefault();
        this.clearAlert("login");

        const email = document.getElementById("loginEmail").value;
        const password = document.getElementById("loginPassword").value;

        if (!email || !password) {
            this.showAlert("login", "Please enter both your email and password.");
            return;
        }

        const btn = document.getElementById("loginSubmitBtn");
        btn.disabled = true;
        btn.innerHTML = "<span>Signing in...</span>";

        try {
            await authService.login(email, password);
            showToast("Signed in successfully!", "success");
        } catch (err) {
            this.showAlert("login", err);
        } finally {
            btn.disabled = false;
            btn.innerHTML = "<span>Login</span>";
        }
    },

    async handleSignup(e) {
        e.preventDefault();
        this.clearAlert("signup");

        const fullName = document.getElementById("signupFullName").value.trim();
        const email = document.getElementById("signupEmail").value.trim();
        const password = document.getElementById("signupPassword").value;
        const confirmPassword = document.getElementById("signupConfirmPassword").value;

        // Validation
        if (!fullName || !email || !password || !confirmPassword) {
            this.showAlert("signup", "Please fill in all required fields.");
            return;
        }

        if (password.length < 6) {
            this.showAlert("signup", "Password must be at least 6 characters long.");
            return;
        }

        if (password !== confirmPassword) {
            this.showAlert("signup", "Passwords do not match.");
            return;
        }

        const btn = document.getElementById("signupSubmitBtn");
        btn.disabled = true;
        btn.innerHTML = "<span>Creating Account...</span>";

        try {
            const result = await authService.signup(email, password, fullName);

            if (result.session) {
                showToast("Account created successfully!", "success");
            } else {
                // If email confirmation is required by Supabase:
                this.showAlert(
                    "signup",
                    "Account created! Please check your email to verify your account before signing in.",
                    "success"
                );
                setTimeout(() => {
                    this.showView("login");
                    document.getElementById("loginEmail").value = email;
                }, 4000);
            }
        } catch (err) {
            this.showAlert("signup", err);
        } finally {
            btn.disabled = false;
            btn.innerHTML = "<span>Create Account</span>";
        }
    },

    async handleForgot(e) {
        e.preventDefault();
        this.clearAlert("forgot");

        const email = document.getElementById("forgotEmail").value.trim();
        if (!email) {
            this.showAlert("forgot", "Please enter your account email address.");
            return;
        }

        const btn = document.getElementById("forgotSubmitBtn");
        btn.disabled = true;
        btn.innerHTML = "<span>Sending Reset Link...</span>";

        try {
            await authService.resetPassword(email);
            this.showAlert("forgot", "Check your email for a password reset link.", "success");
        } catch (err) {
            this.showAlert("forgot", err);
        } finally {
            btn.disabled = false;
            btn.innerHTML = "<span>Send Reset Link</span>";
        }
    }
};

// ==========================================================================
// Application Files Management State & Handlers
// ==========================================================================
let allFiles = [];
let currentTab = "all";
let stagedFile = null;

// Auth Header helper for Spring Boot REST API
function getAuthHeaders(isJson = true) {
    const headers = {};
    if (isJson) {
        headers["Content-Type"] = "application/json";
    }
    const token = authService.getAccessToken();
    if (token) {
        headers["Authorization"] = `Bearer ${token}`;
    }
    return headers;
}

// Navigation Tabs
function switchTab(tab) {
    currentTab = tab;

    // Highlight active sidebar navigation item
    document.querySelectorAll(".nav-item").forEach(btn => {
        btn.classList.toggle("active", btn.getAttribute("data-tab") === tab);
    });

    // Toggle active tab content page
    const tabAllFiles = document.getElementById("tabAllFiles");
    const tabFavorites = document.getElementById("tabFavorites");
    const tabStorageStats = document.getElementById("tabStorageStats");

    if (tabAllFiles) tabAllFiles.classList.toggle("active", tab === "all");
    if (tabFavorites) tabFavorites.classList.toggle("active", tab === "favorites");
    if (tabStorageStats) tabStorageStats.classList.toggle("active", tab === "analytics");

    // Perform page-specific updates
    if (tab === "analytics") {
        loadStats();
    } else {
        renderFiles(allFiles);
    }
}

// Load Files (Row-Level Security isolated per user)
async function loadFiles() {
    const token = authService.getAccessToken();
    if (!token) return;

    try {
        const res = await fetch(`${API_BASE}/files`, {
            headers: getAuthHeaders(true)
        });

        if (res.ok) {
            const result = await res.json();
            allFiles = result.data || [];
            renderFiles(allFiles);
        } else if (res.status === 401) {
            authService.handleAuthRequired();
        } else {
            console.error("Failed to fetch files:", res.status);
        }
    } catch (err) {
        console.error("Load files error:", err);
    }
}

// Generate File Row HTML
function renderFileListHtml(fileList) {
    return fileList.map(file => {
        const fileIcon = getFileIcon(file.extension, file.type);
        const formattedSize = formatBytes(file.size);
        const formattedDate = file.createdAt ? new Date(file.createdAt).toLocaleDateString() : "Just now";

        const starIcon = file.favorite
            ? `<svg width="16" height="16" viewBox="0 0 24 24" fill="#f59e0b" stroke="#f59e0b" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>`
            : `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>`;

        const previewIcon = `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"/><circle cx="12" cy="12" r="3"/></svg>`;
        const downloadIcon = `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" x2="12" y1="15" y2="3"/></svg>`;
        const renameIcon = `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 3a2.85 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z"/></svg>`;
        const deleteIcon = `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18"/><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/></svg>`;

        return `
            <div class="file-row" id="file-${file.id}">
                <div class="file-item-left">
                    <div class="file-icon-box">${fileIcon}</div>
                    <div class="file-text-info">
                        <div class="file-name-text" title="${escapeHtml(file.name)}">${escapeHtml(file.name)}</div>
                        <div class="file-meta-text">${formattedSize} • ${formattedDate} • v${file.version || 1}</div>
                    </div>
                </div>
                <div class="file-actions">
                    <button class="btn-icon" onclick="toggleFavorite('${file.id}')" title="${file.favorite ? 'Remove from favorites' : 'Add to favorites'}">
                        ${starIcon}
                    </button>
                    <button class="btn-icon" onclick="viewFile('${file.id}', '${escapeHtml(file.name)}')" title="Preview file">
                        ${previewIcon}
                    </button>
                    <button class="btn-icon" onclick="downloadFile('${file.id}', '${escapeHtml(file.name)}')" title="Download file">
                        ${downloadIcon}
                    </button>
                    <button class="btn-icon" onclick="renameFile('${file.id}', '${escapeHtml(file.name)}')" title="Rename file">
                        ${renameIcon}
                    </button>
                    <button class="btn-icon" onclick="deleteFile('${file.id}', '${escapeHtml(file.name)}')" title="Delete file" style="color:var(--danger)">
                        ${deleteIcon}
                    </button>
                </div>
            </div>
        `;
    }).join("");
}

// Render Files to DOM across both All Files and Favorites views
function renderFiles(files) {
    const query = (document.getElementById("searchInput")?.value || "").trim().toLowerCase();

    // 1. Render All Files
    const allFiltered = query ? files.filter(f => f.name.toLowerCase().includes(query)) : files;
    const allContainer = document.getElementById("fileListContainer");
    const allEmptyState = document.getElementById("emptyState");
    const allCountBadge = document.getElementById("itemCountBadge");
    const statTotalFiles = document.getElementById("statTotalFiles");

    if (statTotalFiles) statTotalFiles.innerText = files.length;
    if (allCountBadge) allCountBadge.innerText = `${allFiltered.length} items`;

    if (allContainer && allEmptyState) {
        if (allFiltered.length === 0) {
            allContainer.innerHTML = "";
            allEmptyState.style.display = "block";
        } else {
            allEmptyState.style.display = "none";
            allContainer.innerHTML = renderFileListHtml(allFiltered);
        }
    }

    // 2. Render Favorites
    const favFiles = files.filter(f => f.favorite);
    const favFiltered = query ? favFiles.filter(f => f.name.toLowerCase().includes(query)) : favFiles;
    const favContainer = document.getElementById("favFileListContainer");
    const favEmptyState = document.getElementById("favEmptyState");
    const favCountBadge = document.getElementById("favCountBadge");

    if (favCountBadge) favCountBadge.innerText = `${favFiltered.length} items`;

    if (favContainer && favEmptyState) {
        if (favFiltered.length === 0) {
            favContainer.innerHTML = "";
            favEmptyState.style.display = "block";
        } else {
            favEmptyState.style.display = "none";
            favContainer.innerHTML = renderFileListHtml(favFiltered);
        }
    }
}

// Drag & Drop Handling
function initDragAndDrop() {
    const zone = document.getElementById("dropZone");
    if (!zone) return;

    ["dragenter", "dragover"].forEach(eventName => {
        zone.addEventListener(eventName, e => {
            e.preventDefault();
            zone.classList.add("dragover");
        });
    });

    ["dragleave", "drop"].forEach(eventName => {
        zone.addEventListener(eventName, e => {
            e.preventDefault();
            zone.classList.remove("dragover");
        });
    });

    zone.addEventListener("drop", e => {
        if (e.dataTransfer.files.length > 0) {
            stageFile(e.dataTransfer.files[0]);
        }
    });
}

function handleFileSelect(e) {
    if (e.target.files.length > 0) {
        stageFile(e.target.files[0]);
    }
}

function stageFile(file) {
    stagedFile = file;
    document.getElementById("stagingFileName").innerText = file.name;
    document.getElementById("stagingFileSize").innerText = formatBytes(file.size);
    document.getElementById("uploadStaging").style.display = "flex";
}

function cancelStaging() {
    stagedFile = null;
    document.getElementById("fileInput").value = "";
    document.getElementById("uploadStaging").style.display = "none";
}

// Upload File
async function executeUpload() {
    if (!stagedFile) return;

    const token = authService.getAccessToken();
    if (!token) {
        showToast("Please sign in to upload files.", "error");
        authService.handleAuthRequired();
        return;
    }

    const btn = document.getElementById("startUploadBtn");
    btn.disabled = true;
    btn.innerText = "Uploading...";

    const formData = new FormData();
    formData.append("file", stagedFile);

    try {
        const res = await fetch(`${API_BASE}/files/upload`, {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${token}`
            },
            body: formData
        });

        if (res.ok) {
            showToast("File uploaded successfully!", "success");
            cancelStaging();
            await loadFiles();
            await loadStats();
        } else {
            const err = await res.json().catch(() => null);
            showToast(err?.message || "Upload failed.", "error");
        }
    } catch (e) {
        showToast("Network error during upload.", "error");
    } finally {
        btn.disabled = false;
        btn.innerText = "Upload File";
    }
}

// View / Preview File
async function viewFile(fileId, fileName) {
    const token = authService.getAccessToken();
    if (!token) return;

    try {
        const res = await fetch(`${API_BASE}/files/view/${fileId}`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) {
            showToast("Unable to preview file", "error");
            return;
        }
        const blob = await res.blob();
        const url = URL.createObjectURL(blob);
        window.open(url, "_blank");
    } catch (e) {
        showToast("Failed to open file preview", "error");
    }
}

// Download File
async function downloadFile(fileId, fileName) {
    const token = authService.getAccessToken();
    if (!token) return;

    try {
        const res = await fetch(`${API_BASE}/files/download/${fileId}`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) {
            showToast("Download failed", "error");
            return;
        }
        const blob = await res.blob();
        const url = URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = fileName;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
        showToast(`Downloading ${fileName}`, "info");
    } catch (e) {
        showToast("Failed to download file", "error");
    }
}

// Rename File
async function renameFile(fileId, oldName) {
    const newName = prompt("Enter new filename:", oldName);
    if (!newName || newName.trim() === oldName) return;

    try {
        const res = await fetch(`${API_BASE}/files/${fileId}/rename?name=${encodeURIComponent(newName.trim())}`, {
            method: "PUT",
            headers: getAuthHeaders(true)
        });
        if (res.ok) {
            showToast("File renamed successfully", "success");
            await loadFiles();
        } else {
            showToast("Failed to rename file", "error");
        }
    } catch (e) {
        showToast("Error renaming file", "error");
    }
}

// Toggle Favorite
async function toggleFavorite(fileId) {
    try {
        const res = await fetch(`${API_BASE}/files/${fileId}/favorite`, {
            method: "PUT",
            headers: getAuthHeaders(true)
        });
        if (res.ok) {
            await loadFiles();
        }
    } catch (e) {
        showToast("Error updating favorite status", "error");
    }
}

// Delete File
async function deleteFile(fileId, fileName) {
    if (!confirm(`Are you sure you want to delete "${fileName}"?`)) return;

    try {
        const res = await fetch(`${API_BASE}/files/${fileId}`, {
            method: "DELETE",
            headers: getAuthHeaders(true)
        });
        if (res.ok) {
            showToast("File deleted", "success");
            await loadFiles();
            await loadStats();
        } else {
            showToast("Failed to delete file", "error");
        }
    } catch (e) {
        showToast("Error deleting file", "error");
    }
}

// Load Storage Statistics
async function loadStats() {
    const token = authService.getAccessToken();
    if (!token) return;

    try {
        const res = await fetch(`${API_BASE}/stats/storage`, {
            headers: getAuthHeaders(true)
        });
        if (res.ok) {
            const resData = await res.json();
            const stats = resData.data;
            if (stats) {
                const used = stats.storageUsed || 0;
                const limit = stats.storageLimit || (15 * 1024 * 1024 * 1024);
                const percent = Math.min(100, Math.round((used / limit) * 100));
                const free = Math.max(0, limit - used);

                // Update Overview Widget & Top Metric Cards
                const statStorageUsed = document.getElementById("statStorageUsed");
                if (statStorageUsed) statStorageUsed.innerText = formatBytes(used);

                const storagePercentText = document.getElementById("storagePercentText");
                if (storagePercentText) storagePercentText.innerText = `${percent}%`;

                const storageProgressBar = document.getElementById("storageProgressBar");
                if (storageProgressBar) storageProgressBar.style.width = `${percent}%`;

                const storageDetailsText = document.getElementById("storageDetailsText");
                if (storageDetailsText) storageDetailsText.innerText = `${formatBytes(used)} of ${formatBytes(limit)}`;

                // Update Dedicated Storage Analytics Page Elements
                const statsPagePercent = document.getElementById("statsPagePercent");
                if (statsPagePercent) statsPagePercent.innerText = `${percent}%`;

                const statsPageProgressBar = document.getElementById("statsPageProgressBar");
                if (statsPageProgressBar) statsPageProgressBar.style.width = `${percent}%`;

                const statsPageUsedText = document.getElementById("statsPageUsedText");
                if (statsPageUsedText) statsPageUsedText.innerText = formatBytes(used);

                const statsPageFreeText = document.getElementById("statsPageFreeText");
                if (statsPageFreeText) statsPageFreeText.innerText = formatBytes(free);

                const statsPageLimitText = document.getElementById("statsPageLimitText");
                if (statsPageLimitText) statsPageLimitText.innerText = formatBytes(limit);

                // Render File Type Category Breakdown
                renderCategoryCards(stats.sizeByCategory || {}, used);
            }
        }
    } catch (e) {
        console.error("Failed to load storage statistics", e);
    }
}

// Render Categories Breakdown on Storage Analytics Page
function renderCategoryCards(categories, totalUsed) {
    const grid = document.getElementById("statsCategoriesGrid");
    if (!grid) return;

    const categoryMeta = {
        "Documents": {
            color: "#3b82f6",
            bgColor: "rgba(59, 130, 246, 0.1)",
            icon: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#3b82f6" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z"/><polyline points="14 2 14 8 20 8"/><line x1="16" x2="8" y1="13" y2="13"/><line x1="16" x2="8" y1="17" y2="17"/><line x1="10" x2="8" y1="9" y2="9"/></svg>`
        },
        "Images": {
            color: "#06b6d4",
            bgColor: "rgba(6, 182, 212, 0.1)",
            icon: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#06b6d4" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="18" height="18" x="3" y="3" rx="2" ry="2"/><circle cx="9" cy="9" r="2"/><path d="m21 15-3.086-3.086a2 2 0 0 0-2.828 0L6 21"/></svg>`
        },
        "Videos": {
            color: "#8b5cf6",
            bgColor: "rgba(139, 92, 246, 0.1)",
            icon: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#8b5cf6" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="18" height="18" x="3" y="3" rx="2"/><path d="m10 9 5 3-5 3z"/></svg>`
        },
        "Audio": {
            color: "#ec4899",
            bgColor: "rgba(236, 72, 153, 0.1)",
            icon: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#ec4899" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 18V5l12-2v13"/><circle cx="6" cy="18" r="3"/><circle cx="18" cy="16" r="3"/></svg>`
        },
        "Code": {
            color: "#10b981",
            bgColor: "rgba(16, 185, 129, 0.1)",
            icon: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/></svg>`
        },
        "Archives": {
            color: "#f59e0b",
            bgColor: "rgba(245, 158, 11, 0.1)",
            icon: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#f59e0b" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="21 8 21 21 3 21 3 8"/><rect width="22" height="5" x="1" y="3"/><line x1="10" x2="14" y1="12" y2="12"/></svg>`
        },
        "Other": {
            color: "#64748b",
            bgColor: "rgba(100, 116, 139, 0.1)",
            icon: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#64748b" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z"/><polyline points="14 2 14 8 20 8"/></svg>`
        }
    };

    const keys = ["Documents", "Images", "Videos", "Audio", "Code", "Archives", "Other"];
    grid.innerHTML = keys.map(cat => {
        const bytes = categories[cat] || 0;
        const meta = categoryMeta[cat] || categoryMeta["Other"];
        const share = totalUsed > 0 ? Math.round((bytes / totalUsed) * 100) : 0;

        return `
            <div class="category-card">
                <div class="category-card-top">
                    <div class="category-icon-box" style="background: ${meta.bgColor};">
                        ${meta.icon}
                    </div>
                    <div>
                        <div class="category-name">${cat}</div>
                        <div style="font-size: 12px; color: var(--text-tertiary);">${share}% of stored files</div>
                    </div>
                </div>
                <div class="category-stats-row">
                    <span>${formatBytes(bytes)}</span>
                    <span>${share}%</span>
                </div>
                <div class="category-progress-bg">
                    <div class="category-progress-fill" style="width: ${share}%; background: ${meta.color};"></div>
                </div>
            </div>
        `;
    }).join("");
}

// Search Filter
function filterFiles() {
    renderFiles(allFiles);
}

// Helpers
function getFileIcon(ext, type) {
    ext = (ext || "").toLowerCase();
    if (["png", "jpg", "jpeg", "gif", "svg", "webp"].includes(ext)) {
        return `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#0ea5e9" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="18" height="18" x="3" y="3" rx="2" ry="2"/><circle cx="9" cy="9" r="2"/><path d="m21 15-3.086-3.086a2 2 0 0 0-2.828 0L6 21"/></svg>`;
    }
    if (["pdf"].includes(ext)) {
        return `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#ef4444" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z"/><polyline points="14 2 14 8 20 8"/><path d="M9 13v-1a2 2 0 0 1 2-2h0a2 2 0 0 1 2 2v1"/></svg>`;
    }
    if (["zip", "tar", "gz", "rar", "7z"].includes(ext)) {
        return `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#f59e0b" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="21 8 21 21 3 21 3 8"/><rect width="22" height="5" x="1" y="3"/><line x1="10" x2="14" y1="12" y2="12"/></svg>`;
    }
    if (["mp4", "mkv", "mov", "avi"].includes(ext)) {
        return `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#8b5cf6" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="18" height="18" x="3" y="3" rx="2"/><path d="m10 9 5 3-5 3z"/></svg>`;
    }
    if (["mp3", "wav", "ogg"].includes(ext)) {
        return `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#ec4899" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 18V5l12-2v13"/><circle cx="6" cy="18" r="3"/><circle cx="18" cy="16" r="3"/></svg>`;
    }
    if (["js", "ts", "html", "css", "py", "java", "json", "sql"].includes(ext)) {
        return `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/></svg>`;
    }
    return `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#64748b" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z"/><polyline points="14 2 14 8 20 8"/></svg>`;
}

function formatBytes(bytes, decimals = 1) {
    if (!bytes || bytes === 0) return "0 Bytes";
    const k = 1024;
    const dm = decimals < 0 ? 0 : decimals;
    const sizes = ["Bytes", "KB", "MB", "GB", "TB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + " " + sizes[i];
}

function escapeHtml(str) {
    if (!str) return "";
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
}

function showToast(message, type = "info") {
    const container = document.getElementById("toastContainer");
    if (!container) return;
    const toast = document.createElement("div");
    toast.className = `toast ${type}`;
    toast.innerText = message;
    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = "0";
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

// ==========================================================================
// App Startup Hook
// ==========================================================================
window.addEventListener("DOMContentLoaded", async () => {
    initDragAndDrop();
    await authService.init();
});