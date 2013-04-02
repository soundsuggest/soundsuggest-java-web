d3.json("data/json/recommendations.json", function(error, data) {
    new Whitebox(data);
});
