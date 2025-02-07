document.addEventListener("DOMContentLoaded", function () {
    console.log("JavaScript Loaded");

    var modal = document.getElementById("changePasswordModal");

    if (!modal) {
        console.error("Modal element not found!");
        return;
    }

    modal.addEventListener('shown.bs.modal', function (event) {
        console.log("Modal shown event triggered");

        var button = event.relatedTarget;
        if (!button) {
            console.error("Button that triggered the modal not found!");
            return;
        }

        var userId = button.getAttribute('data-user-id');
        if (!userId || isNaN(userId)) {
            console.error("Invalid User ID found on button!");
            return;
        }

        console.log("User ID:", userId);
        setFormAction(userId);
    });
});

function setFormAction(userId) {
    var form = document.getElementById("changePasswordForm");
    if (form) {
        form.action = "/user/" + userId + "/change-password";
        console.log("Form action set to:", form.action);
    } else {
        console.error("Form element not found!");
    }
}