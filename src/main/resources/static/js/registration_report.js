//// Search Functionality
//document.getElementById("searchBtn").addEventListener("click", function() {
//    let searchQuery = document.getElementById("searchInput").value.toLowerCase();
//    let tableRows = document.querySelectorAll("#userTable tbody tr");
//
//    tableRows.forEach(row => {
//        let userId = row.cells[1].textContent.toLowerCase();
//        let userName = row.cells[2].textContent.toLowerCase();
//
//        if (userId.includes(searchQuery) || userName.includes(searchQuery)) {
//            row.style.display = "";
//        } else {
//            row.style.display = "none";
//        }
//    });
//});
