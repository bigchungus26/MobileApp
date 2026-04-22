<?php
include 'connection.php';

$user_id = $_GET['user_id'];
$today = date('Y-m-d');

$stmt = $conn->prepare("SELECT id, title, duration_minutes, calories, intensity, completed FROM workouts WHERE user_id = ? AND date = ?");
$stmt->bind_param("is", $user_id, $today);
$stmt->execute();
$result = $stmt->get_result();

$workouts = array();
while ($row = $result->fetch_assoc()) {
    array_push($workouts, $row);
}

echo json_encode($workouts);

$stmt->close();
$conn->close();
?>
