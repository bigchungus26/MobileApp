<?php
include 'connection.php';

$user_id = $_GET['user_id'];

$stmt = $conn->prepare("SELECT id, title, description, category, frequency, streak FROM habits WHERE user_id = ? AND active = 1");
$stmt->bind_param("i", $user_id);
$stmt->execute();
$result = $stmt->get_result();

$habits = array();
while ($row = $result->fetch_assoc()) {
    array_push($habits, $row);
}

echo json_encode($habits);

$stmt->close();
$conn->close();
?>
