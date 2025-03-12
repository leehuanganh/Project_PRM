<?php
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Authorization");
header("Content-Type: application/json; charset=UTF-8");

$servername = "localhost";
$username = "root";  // Thay đổi nếu có tài khoản khác
$password = "";
$database = "prm";

$conn = new mysqli($servername, $username, $password, $database);

// Đảm bảo kết nối MySQL dùng UTF-8
$conn->set_charset("utf8mb4");

if ($conn->connect_error) {
    die(json_encode(["success" => false, "message" => "Kết nối thất bại: " . $conn->connect_error]));
}
?>
