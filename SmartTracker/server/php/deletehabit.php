<?php
require_once __DIR__ . '/db.php';

$habitId = isset($_POST['habit_id']) ? (int) $_POST['habit_id'] : 0;
$userId  = isset($_POST['user_id'])  ? (int) $_POST['user_id']  : 0;

if ($habitId <= 0 || $userId <= 0) {
    http_response_code(400);
    echo 'Missing required fields';
    exit;
}

$stmt = $conn->prepare("DELETE FROM habits WHERE id = ? AND user_id = ?");
$stmt->bind_param('ii', $habitId, $userId);
$stmt->execute();

echo 'Habit deleted';
