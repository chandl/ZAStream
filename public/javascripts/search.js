function search(){
    var searchTerm = document.getElementById("searchInput").value;
    window.location = "/search/"+searchTerm;
}