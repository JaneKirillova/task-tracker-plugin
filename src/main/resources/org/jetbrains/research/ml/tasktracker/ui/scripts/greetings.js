function checkInputFields() {
    var nameField = document.getElementById('name-field').value
    if (nameField === "") {
        alert("Incorrect Name")
        return false
    }
    var validEmailRegex = /^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*$/;
    var emailField = document.getElementById('email-field').value
    if (!emailField.match(validEmailRegex)) {
        alert("Incorrect Email")
        return false
    }
    return true
}