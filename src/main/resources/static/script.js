const API_BASE = "http://localhost:8080";
let currentUser = null;
let currentToken = localStorage.getItem("cloudstorage_token") || null;
let allFiles = [];
let currentTab = "all";
let stagedFile = null;

// Initialize on load
window.addEventListener("DOMContentLoaded", async () => {
    initDragAndDrop();
    if (currentToken) {
        await fetchUserProfile();
    } else {
        updateGuestUI();
    }
    await loadFiles();
    await loadStats();
});

// Auth Headers helper
function getAuthHeaders(isJson = true) {
    const headers = {};
    if (isJson) {
        headers["Content-Type"] = "application/json";
    }
    if (currentToken) {
        headers["Authorization"] = `Bearer ${currentToken}`;
    }
    return headers;
}

// Fetch Profile
async function fetchUserProfile() {
    try {
        const res = await fetch(`${API_BASE}/users/me`, {
            headers: getAuthHeaders(true)
        });
        if (res.ok) {
            const data = await res.json();
            currentUser = data.data;
            updateUserUI();
        } else {
            handleSessionExpired();
        }
    } catch (e) {
        console.error("Failed to load user profile", e);
    }
}

function updateUserUI() {
    if (!currentUser) return;
    document.getElementById("userAvatar").innerText = (currentUser.firstName || currentUser.username || "U")[0].toUpperCase();
    document.getElementById("userNameLabel").innerText = `${currentUser.firstName || ''} ${currentUser.lastName || ''}`.trim() || currentUser.username;
    document.getElementById("userEmailLabel").innerText = currentUser.email || "";
    document.getElementById("welcomeGreeting").innerText = `Welcome back, ${currentUser.firstName || currentUser.username} 👋`;
    document.getElementById("logoutBtn").style.display = "inline-flex";
    document.getElementById("authActionBtn").innerText = "👤";
}

function updateGuestUI() {
    currentUser = null;
    document.getElementById("userAvatar").innerText = "G";
    document.getElementById("userNameLabel").innerText = "Guest Mode";
    document.getElementById("userEmailLabel").innerText = "Sign in to manage files";
    document.getElementById("welcomeGreeting").innerText = "Welcome to CloudStorage 👋";
    document.getElementById("logoutBtn").style.display = "none";
    document.getElementById("authActionBtn").innerText = "🔑";
}

function handleSessionExpired() {
    localStorage.removeItem("cloudstorage_token");
    currentToken = null;
    updateGuestUI();
}

// Quick 1-Click Demo Login
async function quickDemoLogin() {
    showToast("Authenticating demo account...", "info");
    const demoPayload = {
        usernameOrEmail: "sohel",
        password: "password123"
    };

    try {
        let res = await fetch(`${API_BASE}/auth/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(demoPayload)
        });

        // If not registered yet, auto register first
        if (!res.ok) {
            const regPayload = {
                firstName: "Sohel",
                lastName: "Shaik",
                username: "sohel",
                email: "sohel@cloudstorage.local",
                password: "password123"
            };
            const regRes = await fetch(`${API_BASE}/auth/register`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(regPayload)
            });

            if (regRes.ok) {
                // Now log in
                res = await fetch(`${API_BASE}/auth/login`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(demoPayload)
                });
            }
        }

        if (res.ok) {
            const data = await res.json();
            currentToken = data.data.accessToken;
            localStorage.setItem("cloudstorage_token", currentToken);
            currentUser = data.data.user;
            updateUserUI();
            closeAuthModal();
            showToast("Logged in successfully as Demo User!", "success");
            await loadFiles();
            await loadStats();
        } else {
            showToast("Demo login failed.", "error");
        }
    } catch (e) {
        showToast("Error connecting to server", "error");
    }
}

// Switch between My Files, Favorites, Stats
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

// Load Files List
async function loadFiles() {
    if (!currentToken) {
        // Prompt for demo login if no token
        renderEmptyState("Please sign in or use 1-Click Demo Login to view files.");
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/files`, {
            headers: getAuthHeaders(true)
        });

        if (res.ok) {
            const result = await res.json();
            allFiles = result.data || [];
            renderFiles(allFiles);
        } else if (res.status === 401) {
            handleSessionExpired();
            renderEmptyState("Session expired. Please sign in again.");
        }
    } catch (err) {
        console.error("Load files error:", err);
        renderEmptyState("Could not connect to CloudStorage server.");
    }
}

// Render Files
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

function renderEmptyState(message) {
    const container = document.getElementById("fileListContainer");
    const emptyState = document.getElementById("emptyState");
    container.innerHTML = "";
    emptyState.style.display = "block";
    emptyState.querySelector("p").innerText = message;
    document.getElementById("itemCountBadge").innerText = "0 items";
}

// Drag & Drop Handling
function initDragAndDrop() {
    const zone = document.getElementById("dropZone");

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

// Execute Upload
async function executeUpload() {
    if (!stagedFile) return;

    if (!currentToken) {
        showToast("Please sign in or use Quick Demo Login to upload.", "error");
        toggleAuthModal();
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
                "Authorization": `Bearer ${currentToken}`
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
    if (!currentToken) return;
    try {
        const res = await fetch(`${API_BASE}/files/view/${fileId}`, {
            headers: { "Authorization": `Bearer ${currentToken}` }
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
    if (!currentToken) return;
    try {
        const res = await fetch(`${API_BASE}/files/download/${fileId}`, {
            headers: { "Authorization": `Bearer ${currentToken}` }
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
            showToast("File moved to trash", "success");
            await loadFiles();
            await loadStats();
        } else {
            showToast("Failed to delete file", "error");
        }
    } catch (e) {
        showToast("Error deleting file", "error");
    }
}

// Load Storage Stats
async function loadStats() {
    if (!currentToken) return;
    try {
        const res = await fetch(`${API_BASE}/stats/storage`, {
            headers: getAuthHeaders(true)
        });
        if (res.ok) {
            const resData = await res.json();
            const stats = resData.data;
            if (stats) {
                const used = stats.storageUsed || 0;
                const limit = stats.storageLimit || (500 * 1024 * 1024);
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

// Auth Modal Controls
let authMode = "login";
function toggleAuthModal() {
    const modal = document.getElementById("authModal");
    modal.style.display = modal.style.display === "none" ? "flex" : "none";
}

function closeAuthModal() {
    document.getElementById("authModal").style.display = "none";
}

function switchAuthTab(mode) {
    authMode = mode;
    document.getElementById("tabLoginBtn").classList.toggle("active", mode === "login");
    document.getElementById("tabRegisterBtn").classList.toggle("active", mode === "register");
    document.getElementById("nameGroup").style.display = mode === "register" ? "block" : "none";
    document.getElementById("emailGroup").style.display = mode === "register" ? "block" : "none";
    document.getElementById("authModalTitle").innerText = mode === "login" ? "Sign In to CloudStorage" : "Create CloudStorage Account";
    document.getElementById("authSubmitBtn").innerText = mode === "login" ? "Sign In" : "Create Account";
}

async function handleAuthSubmit(e) {
    e.preventDefault();
    const username = document.getElementById("authUsername").value.trim();
    const password = document.getElementById("authPassword").value;

    if (authMode === "login") {
        try {
            const res = await fetch(`${API_BASE}/auth/login`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ usernameOrEmail: username, password: password })
            });

            const data = await res.json();
            if (res.ok) {
                currentToken = data.data.accessToken;
                localStorage.setItem("cloudstorage_token", currentToken);
                currentUser = data.data.user;
                updateUserUI();
                closeAuthModal();
                showToast("Welcome back!", "success");
                await loadFiles();
                await loadStats();
            } else {
                showToast(data.message || "Invalid credentials", "error");
            }
        } catch (err) {
            showToast("Server connection error", "error");
        }
    } else {
        const firstName = document.getElementById("regFirstName").value.trim();
        const lastName = document.getElementById("regLastName").value.trim();
        const email = document.getElementById("regEmail").value.trim();

        try {
            const res = await fetch(`${API_BASE}/auth/register`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ firstName, lastName, username, email, password })
            });

            const data = await res.json();
            if (res.ok) {
                showToast("Account created! Logging in...", "success");
                // Auto login
                switchAuthTab("login");
                document.getElementById("authUsername").value = username;
                document.getElementById("authPassword").value = password;
                document.getElementById("authForm").dispatchEvent(new Event("submit"));
            } else {
                showToast(data.message || "Registration failed", "error");
            }
        } catch (err) {
            showToast("Server connection error", "error");
        }
    }
}

function handleLogout() {
    handleSessionExpired();
    showToast("Signed out", "info");
    loadFiles();
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
    const toast = document.createElement("div");
    toast.className = `toast ${type}`;
    toast.innerText = message;
    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = "0";
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}