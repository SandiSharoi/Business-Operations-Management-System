 // Add JavaScript for any additional interactive behavior
 document.addEventListener("DOMContentLoaded", function () {
     const form = document.querySelector("form");

     form.addEventListener("submit", function (e) {
         const email = document.getElementById("email").value;
         const password = document.getElementById("password").value;

         if (!email || !password) {
             alert("Please enter both email and password!");
             e.preventDefault(); // Prevent form submission
         }
     });
 });
