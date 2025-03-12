<?php
include 'config.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $input = json_decode(file_get_contents("php://input"), true);
    
    if (!isset($input['user_id'])) {
        echo json_encode(["success" => false, "message" => "Thiếu ID người dùng"], JSON_UNESCAPED_UNICODE);
        exit();
    }

    $user_id = $input['user_id'];

    $stmt = $conn->prepare("SELECT id, category, amount, date, note FROM transactions WHERE user_id = ?");
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

$conn->close();
?>
