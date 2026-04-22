<?php
include 'connection.php';

$habit_id = $_POST['habit_id'];
$user_id = $_POST['user_id'];

$stmt = $conn->prepare("UPDATE habits SET active = 0 WHERE id = ? AND user_id = ?");
$stmt->bind_param("ii", $habit_id, $user_id);

if ($stmt->execute()) {
    echo json_encode(array("success" => true, "message" => "Habit deleted"));
} else {
    echo json_encode(array("success" => false, "message" => "Failed to delete habit"));
}

$stmt->close();
$conn->close();
?>
