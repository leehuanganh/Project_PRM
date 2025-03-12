<?php
include 'config.php';

// Xử lý request OPTIONS (CORS Preflight)
if ($_SERVER['REQUEST_METHOD'] == 'OPTIONS') {
    http_response_code(200);
    exit();
}

// Kiểm tra phương thức request có phải POST không
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405); // Method Not Allowed
    echo json_encode(["success" => false, "message" => "Chỉ hỗ trợ phương thức POST"], JSON_UNESCAPED_UNICODE);
    exit();
}

// Lấy dữ liệu JSON gửi từ client
$inputJSON = file_get_contents("php://input");
$input = json_decode($inputJSON, true);

// Kiểm tra dữ liệu đầu vào có hợp lệ không
if (!$input || !isset($input['action'])) {
    http_response_code(400); // Bad Request
    echo json_encode(["success" => false, "message" => "Thiếu hành động hoặc dữ liệu không hợp lệ"], JSON_UNESCAPED_UNICODE);
    exit();
}

$action = $input['action'];

switch ($action) {
    case "register":
        registerUser($conn, $input);
        break;
    case "login":
        loginUser($conn, $input);
        break;
    default:
        http_response_code(400); // Bad Request
        echo json_encode(["success" => false, "message" => "Hành động không hợp lệ"], JSON_UNESCAPED_UNICODE);
}

// 🔹 Hàm đăng ký người dùng
function registerUser($conn, $input) {
    if (empty($input['email']) || empty($input['password'])) {
        http_response_code(400);
        echo json_encode(["success" => false, "message" => "Thiếu email hoặc mật khẩu"], JSON_UNESCAPED_UNICODE);
        return;
    }

    $email = trim($input['email']);
    $password = password_hash($input['password'], PASSWORD_DEFAULT);

    // Kiểm tra xem email đã tồn tại chưa
    $stmt = $conn->prepare("SELECT id FROM users WHERE email = ?");
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $stmt->store_result();

    if ($stmt->num_rows > 0) {
        http_response_code(409); // Conflict
        echo json_encode(["success" => false, "message" => "Email đã tồn tại"], JSON_UNESCAPED_UNICODE);
        $stmt->close();
        return;
    }
    $stmt->close();

    // Thêm người dùng vào database
    $stmt = $conn->prepare("INSERT INTO users (email, password) VALUES (?, ?)");
    $stmt->bind_param("ss", $email, $password);
    
    if ($stmt->execute()) {
        http_response_code(201); // Created
        echo json_encode(["success" => true, "message" => "Đăng ký thành công"], JSON_UNESCAPED_UNICODE);
    } else {
        http_response_code(500); // Internal Server Error
        echo json_encode(["success" => false, "message" => "Đăng ký thất bại"], JSON_UNESCAPED_UNICODE);
    }

    $stmt->close();
}

// 🔹 Hàm đăng nhập người dùng
function loginUser($conn, $input) {
    if (empty($input['email']) || empty($input['password'])) {
        http_response_code(400);
        echo json_encode(["success" => false, "message" => "Thiếu email hoặc mật khẩu"], JSON_UNESCAPED_UNICODE);
        return;
    }

    $email = trim($input['email']);
    $password = $input['password'];

    // Lấy mật khẩu từ database
    $stmt = $conn->prepare("SELECT id, password FROM users WHERE email = ?");
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $stmt->store_result();

    if ($stmt->num_rows > 0) {
        $stmt->bind_result($id, $hashed_password);
        $stmt->fetch();

        if (password_verify($password, $hashed_password)) {
            http_response_code(200); // OK
            echo json_encode(["success" => true, "message" => "Đăng nhập thành công", "user_id" => $id], JSON_UNESCAPED_UNICODE);
        } else {
            http_response_code(401); // Unauthorized
            echo json_encode(["success" => false, "message" => "Sai mật khẩu"], JSON_UNESCAPED_UNICODE);
        }
    } else {
        http_response_code(404); // Not Found
        echo json_encode(["success" => false, "message" => "Email không tồn tại"], JSON_UNESCAPED_UNICODE);
    }

    $stmt->close();
}

$conn->close();
?>
