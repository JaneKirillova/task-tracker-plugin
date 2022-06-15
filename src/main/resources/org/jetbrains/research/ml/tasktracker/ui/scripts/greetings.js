function checkInputFields() {
    var checked = document.getElementById('agreement').checked;
    if (!checked) {
        alert("Please select \"I agree to terms\" to submit.");
        return false;
    }
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