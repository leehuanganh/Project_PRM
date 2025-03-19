<?php
header("Content-Type: application/json");
require_once "config.php";

$response = ["success" => false];

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $action = $_REQUEST["action"] ?? "";

    switch ($action) {
        case "get_unsynced_transactions":
            $user_id = $_POST["user_id"] ?? null;
            if (!$user_id) {
                $response["error"] = "Thiếu ID người dùng";
                break;
            }
            $response = getUnsyncedTransactions($conn, $user_id);
            break;

        case "sync_transactions":
            $user_id = $_POST["user_id"] ?? null;
            $transactions = $_POST["transactions"] ?? null;

            if (!$user_id || !$transactions || !is_array($transactions)) {
                $response["error"] = "Dữ liệu không hợp lệ";
                break;
            }

            $response = syncTransactions($conn, $user_id, $transactions);
            break;

        default:
            $response["error"] = "Hành động không hợp lệ!";
    }
}

echo json_encode($response, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
$conn->close();

/** ====================================
 * 🟢 Lấy giao dịch chưa đồng bộ
 * ==================================== */
function getUnsyncedTransactions($conn, $user_id) {
    $stmt = $conn->prepare("SELECT id, category, amount, date, note, type, synced FROM transactions WHERE user_id = ? AND synced = 0 ORDER BY date DESC");
    $stmt->bind_param("i", $user_id);
    $stmt->execute();
    $result = $stmt->get_result();

    $transactions = [];
    while ($row = $result->fetch_assoc()) {
        $transactions[] = $row;
    }

    return ["success" => true, "transactions" => $transactions];
}

/** ====================================
 * 🟢 Đồng bộ giao dịch từ thiết bị lên MySQL
 * ==================================== */
function syncTransactions($conn, $user_id, $transactions) {
    $successCount = 0;

    foreach ($transactions as $transaction) {
        $stmt = $conn->prepare("INSERT INTO transactions (user_id, category, amount, date, note, type, synced) 
                                VALUES (?, ?, ?, ?, ?, ?, 1)
                                ON DUPLICATE KEY UPDATE category = VALUES(category), amount = VALUES(amount), date = VALUES(date), note = VALUES(note), type = VALUES(type), synced = 1");
        $stmt->bind_param("isisss", $user_id, $transaction['category'], floatval($transaction['amount']), $transaction['date'], $transaction['note'], $transaction['type']);

        if ($stmt->execute()) {
            $successCount++;
        }
    }

    return ["success" => true, "message" => "Đồng bộ thành công", "transactions_added" => $successCount];
}
