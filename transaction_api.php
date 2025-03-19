<?php
header("Content-Type: application/json");
require_once "config.php";

$response = ["success" => false];

if ($_SERVER["REQUEST_METHOD"] == "POST" || $_SERVER["REQUEST_METHOD"] == "GET") {
    $action = $_REQUEST["action"] ?? "";

    switch ($action) {
        case "get_transactions":
            $user_id = $_REQUEST["user_id"] ?? null;
            $page = $_REQUEST["page"] ?? 1;
            $limit = $_REQUEST["limit"] ?? 20;
            $date_filter = $_REQUEST["date"] ?? "";
            $type_filter = $_REQUEST["type"] ?? "";

            if (!$user_id) {
                $response["error"] = "Thiếu user_id";
                break;
            }

            $response = getTransactions($conn, $user_id, $page, $limit, $date_filter, $type_filter);
            break;

        case "add_transaction":
            $user_id = $_POST["user_id"] ?? null;
            $category = $_POST["category"] ?? null;
            $amount = $_POST["amount"] ?? null;
            $date = $_POST["date"] ?? null;
            $note = $_POST["note"] ?? "";
            $type = $_POST["type"] ?? null;

            if (!$user_id || !$category || !$amount || !$date || !$type) {
                $response["error"] = "Thiếu thông tin giao dịch";
                break;
            }

            $response = addTransaction($conn, $user_id, $category, $amount, $date, $note, $type);
            break;

        case "update_transaction":
            $transaction_id = $_POST["transaction_id"] ?? null;
            $category = $_POST["category"] ?? null;
            $amount = $_POST["amount"] ?? null;
            $date = $_POST["date"] ?? null;
            $note = $_POST["note"] ?? "";
            $type = $_POST["type"] ?? null;

            if (!$transaction_id || !$category || !$amount || !$date || !$type) {
                $response["error"] = "Thiếu thông tin cập nhật";
                break;
            }

            $response = updateTransaction($conn, $transaction_id, $category, $amount, $date, $note, $type);
            break;

        case "delete_transaction":
            $transaction_id = $_POST["transaction_id"] ?? null;

            if (!$transaction_id) {
                $response["error"] = "Thiếu transaction_id";
                break;
            }

            $response = deleteTransaction($conn, $transaction_id);
            break;

        case "sync_transactions":
            $transactions = $_POST["transactions"] ?? [];

            if (!is_array($transactions)) {
                $response["error"] = "Dữ liệu không hợp lệ";
                break;
            }

            $response = syncTransactions($conn, $transactions);
            break;

        default:
            $response["error"] = "Hành động không hợp lệ!";
    }
}

echo json_encode($response, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
$conn->close();

/** ==============================================
 * 🟢 Lấy danh sách giao dịch có phân trang & lọc
 * ============================================== */
function getTransactions($conn, $user_id, $page, $limit, $date_filter, $type_filter) {
    $offset = ($page - 1) * $limit;
    $query = "SELECT * FROM transactions WHERE user_id = ?";
    $params = [$user_id];
    $types = "i";

    if (!empty($date_filter)) {
        $query .= " AND date = ?";
        $params[] = $date_filter;
        $types .= "s";
    }

    if (!empty($type_filter) && in_array($type_filter, ["income", "expense"])) {
        $query .= " AND type = ?";
        $params[] = $type_filter;
        $types .= "s";
    }

    $query .= " ORDER BY date DESC LIMIT ? OFFSET ?";
    $params[] = $limit;
    $params[] = $offset;
    $types .= "ii";

    $stmt = $conn->prepare($query);
    $stmt->bind_param($types, ...$params);
    $stmt->execute();
    $result = $stmt->get_result();

    $transactions = $result->fetch_all(MYSQLI_ASSOC);
    return ["success" => true, "transactions" => $transactions];
}

/** ==================================
 * 🟢 Thêm giao dịch mới vào MySQL
 * ================================== */
function addTransaction($conn, $user_id, $category, $amount, $date, $note, $type) {
    $stmt = $conn->prepare("INSERT INTO transactions (user_id, category, amount, date, note, type, synced) 
                            VALUES (?, ?, ?, ?, ?, ?, 1)");
    $stmt->bind_param("isisss", $user_id, $category, $amount, $date, $note, $type);

    if ($stmt->execute()) {
        return ["success" => true, "transaction_id" => $conn->insert_id];
    } else {
        return ["success" => false, "error" => "Lỗi khi thêm giao dịch"];
    }
}

/** ================================
 * 🟢 Cập nhật giao dịch
 * ================================ */
function updateTransaction($conn, $transaction_id, $category, $amount, $date, $note, $type) {
    $stmt = $conn->prepare("UPDATE transactions 
                            SET category = ?, amount = ?, date = ?, note = ?, type = ? 
                            WHERE id = ?");
    $stmt->bind_param("sisssi", $category, $amount, $date, $note, $type, $transaction_id);

    if ($stmt->execute()) {
        return ["success" => true, "message" => "Cập nhật thành công"];
    } else {
        return ["success" => false, "error" => "Lỗi khi cập nhật giao dịch"];
    }
}

/** ================================
 * 🟢 Xóa giao dịch
 * ================================ */
function deleteTransaction($conn, $transaction_id) {
    $stmt = $conn->prepare("DELETE FROM transactions WHERE id = ?");
    $stmt->bind_param("i", $transaction_id);

    if ($stmt->execute()) {
        return ["success" => true, "message" => "Xóa giao dịch thành công"];
    } else {
        return ["success" => false, "error" => "Lỗi khi xóa giao dịch"];
    }
}

/** ==========================================
 * 🟢 Đồng bộ giao dịch từ thiết bị lên MySQL
 * ========================================== */
function syncTransactions($conn, $transactions) {
    $successCount = 0;
    foreach ($transactions as $transaction) {
        $stmt = $conn->prepare("INSERT INTO transactions (user_id, category, amount, date, note, type, synced) 
                                VALUES (?, ?, ?, ?, ?, ?, 1)");
        $stmt->bind_param("isisss", $transaction['user_id'], $transaction['category'], $transaction['amount'], $transaction['date'], $transaction['note'], $transaction['type']);

        if ($stmt->execute()) {
            $successCount++;
        }
    }

    return ["success" => true, "transactions_added" => $successCount];
}
