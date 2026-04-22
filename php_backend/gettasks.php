<?php
include 'connection.php';

$user_id = $_GET['user_id'];
$today = date('Y-m-d');

$stmt = $conn->prepare("SELECT t.id, t.completed, h.title, h.description FROM tasks t JOIN habits h ON t.habit_id = h.id WHERE t.user_id = ? AND t.date = ? AND h.active = 1");
$stmt->bind_param("is", $user_id, $today);
$stmt->execute();
$result = $stmt->get_result();

$tasks = array();
while ($row = $result->fetch_assoc()) {
    array_push($tasks, $row);
}

echo json_encode($tasks);

$stmt->close();
$conn->close();
?>
