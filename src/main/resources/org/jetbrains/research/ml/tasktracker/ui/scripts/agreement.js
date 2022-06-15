function checkInputFields() {
    var checked = document.getElementById('agreement').checked;
    if (!checked) {
        alert("Please select \"I agree to terms\" to submit.");
        return false;
    }
    return true
}