<?php
header("Content-Type: application/json");
require_once "config.php";

$response = ["success" => false];

if ($_SERVER["REQUEST_METHOD"] == "POST" || $_SERVER["REQUEST_METHOD"] == "GET") {
    $action = $_REQUEST["action"] ?? "";

    switch ($action) {
        case "register":
            $email = $_POST["email"] ?? null;
            $password = $_POST["password"] ?? null;

            if (!$email || !$password) {
                $response["error"] = "Thiếu email hoặc mật khẩu";
                break;
            }

            $response = registerUser($conn, $email, $password);
            break;

        case "login":
            $email = $_POST["email"] ?? null;
            $password = $_POST["password"] ?? null;

            if (!$email || !$password) {
                $response["error"] = "Thiếu email hoặc mật khẩu";
                break;
            }

            $response = loginUser($conn, $email, $password);
            break;

        case "change_password":
            $user_id = $_POST["user_id"] ?? null;
            $old_password = $_POST["old_password"] ?? null;
            $new_password = $_POST["new_password"] ?? null;

            if (!$user_id || !$old_password || !$new_password) {
                $response["error"] = "Thiếu dữ liệu cần thiết";
                break;
            }

            $response = changePassword($conn, $user_id, $old_password, $new_password);
            break;

        case "update_profile":
            $user_id = $_POST["user_id"] ?? null;
            $name = $_POST["name"] ?? null;
            $email = $_POST["email"] ?? null;
            $phone = $_POST["phone"] ?? null;

            if (!$user_id || !$name || !$email || !$phone) {
                $response["error"] = "Thiếu thông tin cập nhật";
                break;
            }

            $response = updateProfile($conn, $user_id, $name, $email, $phone);
            break;

        default:
            $response["error"] = "Hành động không hợp lệ!";
    }
}

echo json_encode($response, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
$conn->close();

/** ================================
 * 🟢 Đăng ký người dùng
 * ================================ */
function registerUser($conn, $email, $password) {
    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        return ["success" => false, "error" => "Email không hợp lệ"];
    }

    $hashed_password = password_hash($password, PASSWORD_DEFAULT);

    $stmt = $conn->prepare("SELECT id FROM users WHERE email = ?");
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $stmt->store_result();

    if ($stmt->num_rows > 0) {
        return ["success" => false, "error" => "Email đã tồn tại"];
    }
    $stmt->close();

    $stmt = $conn->prepare("INSERT INTO users (email, password) VALUES (?, ?)");
    $stmt->bind_param("ss", $email, $hashed_password);

    if ($stmt->execute()) {
        return ["success" => true, "message" => "Đăng ký thành công"];
    } else {
        return ["success" => false, "error" => "Đăng ký thất bại"];
    }
}

/** ================================
 * 🟢 Đăng nhập người dùng
 * ================================ */
function loginUser($conn, $email, $password) {
    $stmt = $conn->prepare("SELECT id, password FROM users WHERE email = ?");
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $stmt->store_result();

    if ($stmt->num_rows > 0) {
        $stmt->bind_result($id, $hashed_password);
        $stmt->fetch();

        if (password_verify($password, $hashed_password)) {
            return ["success" => true, "user_id" => $id, "message" => "Đăng nhập thành công"];
        } else {
            return ["success" => false, "error" => "Sai mật khẩu"];
        }
    } else {
        return ["success" => false, "error" => "Email không tồn tại"];
    }

    $stmt->close();
}

/** ================================
 * 🟢 Đổi mật khẩu
 * ================================ */
function changePassword($conn, $user_id, $old_password, $new_password) {
    $stmt = $conn->prepare("SELECT password FROM users WHERE id = ?");
    $stmt->bind_param("i", $user_id);
    $stmt->execute();
    $stmt->bind_result($hashed_password);
    $stmt->fetch();

    if (!password_verify($old_password, $hashed_password)) {
        return ["success" => false, "error" => "Mật khẩu cũ không đúng"];
    }

    $stmt->close();
    $new_hashed_password = password_hash($new_password, PASSWORD_DEFAULT);
    $stmt = $conn->prepare("UPDATE users SET password = ? WHERE id = ?");
    $stmt->bind_param("si", $new_hashed_password, $user_id);

    if ($stmt->execute()) {
        return ["success" => true, "message" => "Đổi mật khẩu thành công"];
    } else {
        return ["success" => false, "error" => "Lỗi khi đổi mật khẩu"];
    }
}

/** ================================
 * 🟢 Cập nhật thông tin người dùng
 * ================================ */
function updateProfile($conn, $user_id, $name, $email, $phone) {
    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        return ["success" => false, "error" => "Email không hợp lệ"];
    }

    $stmt = $conn->prepare("UPDATE users SET name = ?, email = ?, phone = ? WHERE id = ?");
    $stmt->bind_param("sssi", $name, $email, $phone, $user_id);

    if ($stmt->execute()) {
        return ["success" => true, "message" => "Cập nhật thông tin thành công"];
    } else {
        return ["success" => false, "error" => "Lỗi khi cập nhật thông tin"];
    }
}
