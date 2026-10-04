// ==========================================================================
// CloudStorage - Supabase Auth & Multi-User Data Isolation Client
// ==========================================================================

const API_BASE = window.location.origin;

// Public Supabase Configuration (DO NOT put service_role or DB passwords here)
const SUPABASE_URL = "https://sqatowxytdyauwqlmkcx.supabase.co";
const SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNxYXRvd3h5dGR5YXV3cWxta2N4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTEwOTkwMzgsImV4cCI6MjEwNjY3NTAzOH0.qEPuvdQrndwlVQY3z7g21HvKbzAvaKRmdMI-wJqUCzg";

// Initialize the official Supabase JavaScript Client
const supabase = window.supabase.createClient(SUPABASE_URL, SUPABASE_ANON_KEY);

// ==========================================================================
// Centralized Authentication State
// ==========================================================================
const authService = {
    user: null,
    session: null,
    loading: true,

    async init() {
        try {
            // 1. Check existing Supabase session
            const { data: { session }, error } = await supabase.auth.getSession();
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
            supabase.auth.onAuthStateChange(async (event, newSession) => {
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
            // Hide the initial fullscreen loading overlay
            const overlay = document.getElementById("authLoadingOverlay");
            if (overlay) {
                overlay.style.opacity = "0";
                setTimeout(() => { overlay.style.display = "none"; }, 250);
            }
        }
    },

    async login(email, password) {
        const { data, error } = await supabase.auth.signInWithPassword({
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
        const { data, error } = await supabase.auth.signUp({
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
            await supabase.auth.signOut();
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
        const { data, error } = await supabase.auth.resetPasswordForEmail(email.trim(), {
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
        document.getElementById("welcomeGreeting").innerText = `Welcome back, ${fullName} 👋`;
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
            btn.innerText = "🙈";
        } else {
            input.type = "password";
            btn.innerText = "👁️";
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
    document.querySelectorAll(".nav-item").forEach(btn => {
        btn.classList.toggle("active", btn.getAttribute("data-tab") === tab);
    });

    if (tab === "favorites") {
        document.getElementById("viewSectionTitle").innerText = "⭐ Favorite Files";
    } else {
        document.getElementById("viewSectionTitle").innerText = "📁 My Files";
    }
    renderFiles(allFiles);
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

// Render Files to DOM
function renderFiles(files) {
    const container = document.getElementById("fileListContainer");
    const emptyState = document.getElementById("emptyState");
    const countBadge = document.getElementById("itemCountBadge");

    let filtered = files;
    if (currentTab === "favorites") {
        filtered = files.filter(f => f.favorite);
    }

    const query = document.getElementById("searchInput").value.trim().toLowerCase();
    if (query) {
        filtered = filtered.filter(f => f.name.toLowerCase().includes(query));
    }

    countBadge.innerText = `${filtered.length} items`;
    document.getElementById("statTotalFiles").innerText = allFiles.length;

    if (filtered.length === 0) {
        container.innerHTML = "";
        emptyState.style.display = "block";
        return;
    }

    emptyState.style.display = "none";
    container.innerHTML = filtered.map(file => {
        const fileIcon = getFileIcon(file.extension, file.type);
        const formattedSize = formatBytes(file.size);
        const formattedDate = file.createdAt ? new Date(file.createdAt).toLocaleDateString() : "Just now";

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
                    <button class="btn-icon" onclick="toggleFavorite('${file.id}')" title="${file.favorite ? 'Unstar' : 'Star'}">
                        ${file.favorite ? '⭐' : '☆'}
                    </button>
                    <button class="btn-icon" onclick="viewFile('${file.id}', '${escapeHtml(file.name)}')" title="Preview">
                        👁
                    </button>
                    <button class="btn-icon" onclick="downloadFile('${file.id}', '${escapeHtml(file.name)}')" title="Download">
                        ⬇
                    </button>
                    <button class="btn-icon" onclick="renameFile('${file.id}', '${escapeHtml(file.name)}')" title="Rename">
                        ✏
                    </button>
                    <button class="btn-icon" onclick="deleteFile('${file.id}', '${escapeHtml(file.name)}')" title="Delete" style="color:var(--danger)">
                        🗑
                    </button>
                </div>
            </div>
        `;
    }).join("");
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

                document.getElementById("statStorageUsed").innerText = formatBytes(used);
                document.getElementById("storagePercentText").innerText = `${percent}%`;
                document.getElementById("storageProgressBar").style.width = `${percent}%`;
                document.getElementById("storageDetailsText").innerText = `${formatBytes(used)} of ${formatBytes(limit)}`;
            }
        }
    } catch (e) {
        console.error("Failed to load storage statistics", e);
    }
}

// Search Filter
function filterFiles() {
    renderFiles(allFiles);
}

// Helpers
function getFileIcon(ext, type) {
    ext = (ext || "").toLowerCase();
    if (["png", "jpg", "jpeg", "gif", "svg", "webp"].includes(ext)) return "🖼️";
    if (["pdf"].includes(ext)) return "📕";
    if (["zip", "tar", "gz", "rar", "7z"].includes(ext)) return "📦";
    if (["mp4", "mkv", "mov", "avi"].includes(ext)) return "🎬";
    if (["mp3", "wav", "ogg"].includes(ext)) return "🎵";
    if (["js", "ts", "html", "css", "py", "java", "json", "sql"].includes(ext)) return "💻";
    if (["doc", "docx", "txt", "md"].includes(ext)) return "📝";
    return "📄";
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