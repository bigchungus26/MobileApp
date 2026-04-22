<?php
include 'connection.php';

$workout_id = $_POST['workout_id'];
$user_id = $_POST['user_id'];

$stmt = $conn->prepare("DELETE FROM workouts WHERE id = ? AND user_id = ?");
$stmt->bind_param("ii", $workout_id, $user_id);

if ($stmt->execute()) {
    echo json_encode(array("success" => true, "message" => "Workout deleted"));
} else {
    echo json_encode(array("success" => false, "message" => "Failed to delete workout"));
}

$stmt->close();
$conn->close();
?>
