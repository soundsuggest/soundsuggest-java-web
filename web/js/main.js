d3.json("data/json/recommendations.json", function(error, data) {
    new Whitebox(data);
});

function addRecommendation(bandname) {
    alert("Add recommendation " + bandname + " to my library.");
}