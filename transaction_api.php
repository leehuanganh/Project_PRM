<?php
include 'config.php';

// Xử lý request OPTIONS (cho CORS preflight request)
if ($_SERVER['REQUEST_METHOD'] == 'OPTIONS') {
    http_response_code(200);
    exit();
}

// Kiểm tra phương thức request
$method = $_SERVER['REQUEST_METHOD'];

if ($method == 'POST') {
    $input = json_decode(file_get_contents("php://input"), true);
    
    if (!isset($input['action'])) {
        echo json_encode(["success" => false, "message" => "Thiếu hành động"], JSON_UNESCAPED_UNICODE);
        exit();
    }

    $action = $input['action'];

    switch ($action) {
        case "add_transaction":
            addTransaction($conn, $input);
            break;
        case "update_transaction":
            updateTransaction($conn, $input);
            break;
        case "delete_transaction":
            deleteTransaction($conn, $input);
            break;
        default:
            echo json_encode(["success" => false, "message" => "Hành động không hợp lệ"], JSON_UNESCAPED_UNICODE);
    }
} elseif ($method == 'GET' && isset($_GET['action']) && $_GET['action'] == 'get_transactions') {
    if (!isset($_GET['user_id'])) {
        echo json_encode(["success" => false, "message" => "Thiếu user_id"], JSON_UNESCAPED_UNICODE);
        exit();
    }
    getTransactions($conn, $_GET['user_id']);
} else {
    echo json_encode(["success" => false, "message" => "Chỉ hỗ trợ phương thức POST hoặc GET"], JSON_UNESCAPED_UNICODE);
}

// 🔹 Hàm lấy danh sách giao dịch
function getTransactions($conn, $user_id) {
    $stmt = $conn->prepare("SELECT id, category, amount, date, note FROM transactions WHERE user_id = ? ORDER BY date DESC");
    $stmt->bind_param("i", $user_id);
    $stmt->execute();
    $result = $stmt->get_result();

    $transactions = [];
    while ($row = $result->fetch_assoc()) {
        $transactions[] = $row;
    }

    echo json_encode(["success" => true, "transactions" => $transactions], JSON_UNESCAPED_UNICODE);
    $stmt->close();
}

// 🔹 Hàm thêm giao dịch
function addTransaction($conn, $input) {
    if (!isset($input['user_id'], $input['category'], $input['amount'], $input['date'], $input['note'])) {
        echo json_encode(["success" => false, "message" => "Thiếu thông tin giao dịch"], JSON_UNESCAPED_UNICODE);
        return;
    }

    $stmt = $conn->prepare("INSERT INTO transactions (user_id, category, amount, date, note) VALUES (?, ?, ?, ?, ?)");
    $stmt->bind_param("isiss", $input['user_id'], $input['category'], $input['amount'], $input['date'], $input['note']);

    if ($stmt->execute()) {
        echo json_encode(["success" => true, "message" => "Giao dịch đã thêm"]);
    } else {
        echo json_encode(["success" => false, "message" => "Lỗi khi thêm giao dịch"]);
    }

    $stmt->close();
}

// 🔹 Hàm cập nhật giao dịch
function updateTransaction($conn, $input) {
    if (!isset($input['transaction_id'], $input['category'], $input['amount'], $input['date'], $input['note'])) {
        echo json_encode(["success" => false, "message" => "Thiếu thông tin cần cập nhật"], JSON_UNESCAPED_UNICODE);
        return;
    }

    $stmt = $conn->prepare("UPDATE transactions SET category = ?, amount = ?, date = ?, note = ? WHERE id = ?");
    $stmt->bind_param("sissi", $input['category'], $input['amount'], $input['date'], $input['note'], $input['transaction_id']);

    if ($stmt->execute()) {
        echo json_encode(["success" => true, "message" => "Cập nhật thành công"]);
    } else {
        echo json_encode(["success" => false, "message" => "Lỗi khi cập nhật"]);
    }

    $stmt->close();
}

// 🔹 Hàm xóa giao dịch
function deleteTransaction($conn, $input) {
    if (!isset($input['transaction_id'])) {
        echo json_encode(["success" => false, "message" => "Thiếu ID giao dịch"], JSON_UNESCAPED_UNICODE);
        return;
    }

    $stmt = $conn->prepare("DELETE FROM transactions WHERE id = ?");
    $stmt->bind_param("i", $input['transaction_id']);

    if ($stmt->execute()) {
        echo json_encode(["success" => true, "message" => "Xóa thành công"]);
    } else {
        echo json_encode(["success" => false, "message" => "Lỗi khi xóa"]);
    }

    $stmt->close();
}

$conn->close();
?>
