<?php
include 'connection.php';

$name = $_POST['name'];
$email = $_POST['email'];
$password = $_POST['password'];

// Check if email already exists
$check = $conn->prepare("SELECT id FROM users WHERE email = ?");
$check->bind_param("s", $email);
$check->execute();
$check->store_result();

if ($check->num_rows > 0) {
    echo json_encode(array("success" => false, "message" => "Email already registered"));
} else {
    $hashed = password_hash($password, PASSWORD_DEFAULT);
    $stmt = $conn->prepare("INSERT INTO users (name, email, password) VALUES (?, ?, ?)");
    $stmt->bind_param("sss", $name, $email, $hashed);

    if ($stmt->execute()) {
        $user_id = $stmt->insert_id;
        echo json_encode(array(
            "success" => true,
            "userId" => $user_id,
            "name" => $name,
            "email" => $email
        ));
    } else {
        echo json_encode(array("success" => false, "message" => "Registration failed"));
    }
    $stmt->close();
}

$check->close();
$conn->close();
?>
