<?php
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Credentials: true");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Authorization");
header("Content-Type: application/json; charset=UTF-8");

$servername = "localhost";
$username = "root";  // Thay đổi nếu có tài khoản MySQL khác
$password = "";
$database = "prm";

$conn = new mysqli($servername, $username, $password, $database);

// ✅ Đảm bảo kết nối MySQL dùng UTF-8
if (!$conn->set_charset("utf8mb4")) {
    die(json_encode(["success" => false, "message" => "Lỗi thiết lập charset: " . $conn->error]));
}

// ✅ Kiểm tra kết nối database
if ($conn->connect_error) {
    die(json_encode(["success" => false, "message" => "Kết nối thất bại: " . $conn->connect_error]));
}

/** ======================================================
 * 🔹 Hàm trả về JSON chuẩn (kiểm tra lỗi `json_encode()`)
 * ====================================================== */
function response($status, $message, $data = []) {
    http_response_code($status);
    
    $response = array_merge(["success" => $status < 400, "message" => $message], $data);
    $jsonResponse = json_encode($response, JSON_UNESCAPED_UNICODE);

    if ($jsonResponse === false) {
        http_response_code(500);
        echo json_encode(["success" => false, "message" => "Lỗi mã hóa JSON"]);
    } else {
        echo $jsonResponse;
    }
    exit();
}
?>
