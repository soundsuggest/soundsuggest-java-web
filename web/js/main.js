d3.json("DataService?key=e31c84b07c41b1ef6c34ada12141ef0f&user=noisedriver", function(error, data) {
    new Whitebox(data);
});

function addRecommendation(bandname) {
    alert("Add recommendation " + bandname + " to my library.");
}