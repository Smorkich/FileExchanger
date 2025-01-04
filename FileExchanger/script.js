const API_URL = "http://localhost:8090/api/v1/files"; // Замените на ваш URL

function logMessage(type, message) {
    const timestamp = new Date().toISOString();
    console.log(`[${type}] [${timestamp}] ${message}`);
}

// Загрузка файла
async function uploadFile() {
    const fileInput = document.getElementById('fileInput');
    if (!fileInput.files[0]) {
        alert("Please select a file to upload");
        logMessage("WARN", "Попытка загрузить файл без выбора.");
        return;
    }

    const formData = new FormData();
    formData.append("file", fileInput.files[0]);

    try {
        logMessage("INFO", `Начата загрузка файла: ${fileInput.files[0].name}`);
        const response = await fetch(`${API_URL}/upload`, {
            method: "POST",
            body: formData,
        });
        const message = await response.text();
        alert(message);
        logMessage("INFO", `Файл успешно загружен: ${fileInput.files[0].name}`);
        fetchFiles(); // Обновляем список файлов
    } catch (err) {
        alert("Error uploading file: " + err.message);
        logMessage("ERROR", `Ошибка при загрузке файла: ${err.message}`);
    }
}

// Скачивание файла
async function downloadFile(filename) {
    try {
        logMessage("INFO", `Начато скачивание файла: ${filename}`);
        const response = await fetch(`${API_URL}/download/${filename}`);
        if (!response.ok) {
            throw new Error(`Failed to download file: ${response.statusText}`);
        }
        const blob = await response.blob();

        // Создаем ссылку для скачивания
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement("a");
        link.href = url;
        link.setAttribute("download", filename);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        logMessage("INFO", `Файл успешно скачан: ${filename}`);
    } catch (err) {
        alert("Error downloading file: " + err.message);
        logMessage("ERROR", `Ошибка при скачивании файла: ${filename}, ${err.message}`);
    }
}

// Удаление файла
async function deleteFile(filename) {
    try {
        logMessage("INFO", `Запрос на удаление файла: ${filename}`);
        const response = await fetch(`${API_URL}/delete/${filename}`, {
            method: "DELETE",
        });
        if (!response.ok) {
            throw new Error(`Failed to delete file: ${response.statusText}`);
        }
        alert("File deleted successfully");
        logMessage("INFO", `Файл успешно удалён: ${filename}`);
        await fetchFiles(); // Обновляем список файлов
    } catch (err) {
        alert("Error deleting file: " + err.message);
        logMessage("ERROR", `Ошибка при удалении файла: ${filename}, ${err.message}`);
    }
}

// Получение списка файлов (заглушка для примера)
async function fetchFiles() {
    try {
        logMessage("INFO", "Запрос на получение списка файлов.");
        const response = await fetch(`${API_URL}/list`);
        if (!response.ok) {
            throw new Error(`Failed to fetch files: ${response.statusText}`);
        }
        const files = await response.json();

        const fileList = document.getElementById('fileList');
        fileList.innerHTML = ""; // Очищаем список

        files.forEach((file) => {
            const li = document.createElement("li");
            li.textContent = file;

            const downloadButton = document.createElement("button");
            downloadButton.textContent = "Download";
            downloadButton.onclick = () => downloadFile(file);

            const deleteButton = document.createElement("button");
            deleteButton.textContent = "Delete";
            deleteButton.className = "delete";
            deleteButton.onclick = () => deleteFile(file);

            li.appendChild(downloadButton);
            li.appendChild(deleteButton);
            fileList.appendChild(li);
        });
        logMessage("INFO", `Список файлов обновлён. Всего файлов: ${files.length}`);
    } catch (err) {
        alert("Error fetching files: " + err.message);
        logMessage("ERROR", `Ошибка при получении списка файлов: ${err.message}`);
    }
}

// Загружаем список файлов при загрузке страницы
document.addEventListener("DOMContentLoaded", () => {
    fetchFiles(); // Загружаем список файлов
    const uploadBtn = document.getElementById("uploadBtn");
    uploadBtn.addEventListener("click", uploadFile);
});
