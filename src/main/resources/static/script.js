const API_URL = "http://localhost:8080/files";

async function uploadFile() {

    const fileInput = document.getElementById("fileInput");

    if (fileInput.files.length === 0) {
        alert("Please select a file first.");
        return;
    }

    const formData = new FormData();
    formData.append("file", fileInput.files[0]);

    try {
        const response = await fetch(`${API_URL}/upload`, {
            method: "POST",
            body: formData
        });

        const message = await response.text();

        alert(message);

        fileInput.value = "";

        loadFiles();

    } catch (error) {
        console.error(error);
        alert("Upload failed.");
    }
}

async function loadFiles() {

    try {
        const response = await fetch(API_URL);
        const files = await response.json();

        const fileList = document.getElementById("fileList");

        fileList.innerHTML = "";

        const totalFiles = document.getElementById("totalFiles");
        if (totalFiles) {
            totalFiles.innerText = files.length;
        }

        files.forEach(file => {

            fileList.innerHTML += `
                <div class="file-row">

                    <div class="file-info">
                        <strong>${file.name}</strong>
                        <p>${file.type}</p>
                    </div>

                    <div class="actions">

                        <button class="view-btn"
                            onclick="viewFile('${file.name}')">
                            👁 View
                        </button>

                        <button class="download-btn"
                            onclick="downloadFile('${file.name}')">
                            ⬇ Download
                        </button>

                        <button class="rename-btn"
                            onclick="renameFile('${file.name}')">
                            ✏ Rename
                        </button>

                        <button class="delete-btn"
                            onclick="deleteFile('${file.name}')">
                            🗑 Delete
                        </button>

                    </div>

                </div>
            `;
        });

    } catch (error) {
        console.error(error);
        alert("Could not load files.");
    }
}

function viewFile(fileName) {
    window.open(`${API_URL}/view/${fileName}`, "_blank");
}

function downloadFile(fileName) {
    window.open(`${API_URL}/download/${fileName}`, "_blank");
}

async function deleteFile(fileName) {

    const confirmed = confirm(
        `Delete ${fileName}?`
    );

    if (!confirmed) return;

    try {
        await fetch(`${API_URL}/${fileName}`, {
            method: "DELETE"
        });

        loadFiles();

    } catch (error) {
        console.error(error);
        alert("Delete failed.");
    }
}

async function renameFile(oldName) {

    const newName = prompt(
        "Enter new file name:",
        oldName
    );

    if (!newName || newName === oldName) {
        return;
    }

    try {
        await fetch(
            `${API_URL}/rename?oldName=${encodeURIComponent(oldName)}&newName=${encodeURIComponent(newName)}`,
            {
                method: "PUT"
            }
        );

        loadFiles();

    } catch (error) {
        console.error(error);
        alert("Rename failed.");
    }
}

window.onload = function () {
    loadFiles();
};