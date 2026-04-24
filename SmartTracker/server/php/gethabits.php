<?php
require_once __DIR__ . '/db.php';

$userId = isset($_GET['user_id']) ? (int) $_GET['user_id'] : 0;

$stmt = $conn->prepare(
    "SELECT id, title, description, category, frequency, streak
     FROM habits
     WHERE user_id = ?
     ORDER BY id DESC"
);
$stmt->bind_param('i', $userId);
$stmt->execute();
$result = $stmt->get_result();

$habits = array();
while ($row = $result->fetch_assoc()) {
    $habits[] = $row;
}

header('Content-Type: application/json');
echo json_encode($habits);
