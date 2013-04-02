var w = 500,
    h = w,
    rx = w / 2,
    ry = h / 2,
    userMap = {};

var cluster = d3.layout.cluster()
        .size([360, ry - 120])
        .sort(function(a, b) {
            return d3.ascending(a.key, b.key);
        });

var bundle = d3.layout.bundle();

var line = d3.svg.line.radial()
        .interpolate("bundle")
        .tension(.85)
        .radius(function(d) {
            return d.y;
        })
        .angle(function(d) {
            return d.x / 180 * Math.PI;
        });

var div = d3.select("#chart");

var svg = div.append("svg:svg")
        .attr("width", w)
        .attr("height", h)
        .append("svg:g")
        .attr("transform", "translate(" + rx + "," + ry + ")");

d3.json("data/json/recommendations.json", function(error, data) {
    data.users.forEach(function(user) {
        userMap[user.name] = {
            active  : user.active,
            items : []
        };
        user.owned.forEach(function(item) {
            userMap[user.name].items.push(item.split(".")[1]);
        });
    });
    createGraph(data.items);
    createUserList(data.users);
});

function createGraph(items) {
    var nodes = cluster.nodes(packages.root(items)),
        links = packages.edges(nodes),
        splines = bundle(links);

    var path = svg.selectAll("path.link")
            .data(links)
            .enter().append("svg:path")
            .attr("class", function(d) {
                return "link source-" + d.source.key + " target-" + d.target.key + " link-owner-" + d.owner + ((d.active)?" link-activeuser":"");
            })
            .attr("d", function(d, i) {
                return line(splines[i]);
            });

    svg.selectAll("g.node")
            .data(nodes.filter(function(n) {
                return !n.children;
            }))
            .enter().append("svg:g")
            .attr("class", function(d) {
                var users = "";
                d.owners.forEach(function(i) {
                    users += " node-owner-" + i;
                });
                return "node" + users;
            })
            .attr("id", function(d) {
                return "node-" + d.key;
            })
            .attr("transform", function(d) {
                return "rotate(" + (d.x - 90) + ")translate(" + d.y + ")";
            })
            .append("svg:text")
            .attr("dx", function(d) {
                return d.x < 180 ? 8 : -8;
            })
            .attr("dy", ".31em")
            .attr("text-anchor", function(d) {
                return d.x < 180 ? "start" : "end";
            })
            .attr("transform", function(d) {
                return d.x < 180 ? null : "rotate(180)";
            })
            .text(function(d) {
                return d.key;
            })
            .on("mouseover", mouseoverItem)
            .on("mouseout", mouseoutItem)
            .on("click", clickItem);

    d3.select("input[type=range]").on("change", function() {
        line.tension(this.value / 100);
        path.attr("d", function(d, i) {
            return line(splines[i]);
        });
    });
}

function createUserList(users) {
    var list = "<ul>";
    users.forEach(function(user) {
        var itemClasses = "";
        userMap[user.name].items.forEach(function (item) {
            itemClasses += " user-owns-" + item + " ";
        });
        
        list += "<li "
             + " id=\"user-" + user.name + "\" "
             + " class=\"user " + ((user.active)?" user-activeuser ":" ") + itemClasses + "\" "
             + " onclick=\"clickUser('" + user.name + "');\" "
             + " onmouseover=\"mouseoverUser('" + user.name + "')\" "
             + " onmouseout=\"mouseoutUser('" + user.name + "')\" "
             + ">" + user.name + "</li>";
    });
    
    list += "</ul>";
    jQuery(list).appendTo("#users");
}

var packages = {
    root: function(items) {
        var map = {};
        
        function find(name, data) {
            var node = map[name], i;
            if (!node) {
                node = map[name] = data || {name: name, children: []};
                if (name.length) {
                    node.parent = find(name.substring(0, i = name.lastIndexOf(".")));
                    node.parent.children.push(node);
                    node.key = name.substring(i + 1);
                }
            }
            return node;
        }
        
        items.forEach(function(d) {
            find(d.name, d);
        });
        
        return map[""];
    },
    edges: function(nodes) {
        var map = {},
            edges = [];

        nodes.forEach(function(d) {
            map[d.name] = d;
        });

        nodes.forEach(function(d) {
            if (d.edges)
                d.edges.forEach(function(i) {
                    var temp    = i.split(".user.");
                    var target  = temp[0];
                    var owner   = temp[1];
                    
                    edges.push({
                        source  : map[d.name],
                        target  : map[target],
                        owner   : owner,
                        active  : userMap[owner].active
                    });
                });
        });
        
        return edges;
    }
};

function mouseoverItem(d) {
    svg.selectAll("path.link.target-" + d.key)
            .classed("target", true)
            .each(updateNodes("source", true));

    svg.selectAll("path.link.source-" + d.key)
            .classed("source", true)
            .each(updateNodes("target", true));
    
    jQuery(".user-owns-" + d.key)
        .addClass("user-item-mouseover");
}

function mouseoutItem(d) {
    svg.selectAll("path.link.source-" + d.key)
            .classed("source", false)
            .each(updateNodes("target", false));

    svg.selectAll("path.link.target-" + d.key)
            .classed("target", false)
            .each(updateNodes("source", false));
    
    jQuery(".user-owns-" + d.key)
        .removeClass("user-item-mouseover");
}

function updateNodes(name, value) {
    return function(d) {
        if (value) this.parentNode.appendChild(this);
        svg.select("#node-" + d[name].key).classed(name, value);
    };
}

function mouseoverUser(user) {
    svg.selectAll("path.link.link-owner-" + user)
        .classed("target", true);
    svg.selectAll("path.link.link-owner-" + user)
        .classed("source", true);
}

function mouseoutUser(user) {
    svg.selectAll("path.link.link-owner-" + user)
        .classed("target", false);
    svg.selectAll("path.link.link-owner-" + user)
        .classed("source", false);
}

function clickItem(d) {
    if (svg.selectAll(".item-clicked").empty() && jQuery(".user-clicked").length === 0) {
        svg.selectAll("path.link.target-" + d.key)
            .classed("link-item-clicked", true);
        svg.selectAll("path.link.source-" + d.key)
            .classed("link-item-clicked", true);
        svg.select("#node-" + d.key)
            .classed("item-clicked", true);
        jQuery(createItemInfo(d))
            .appendTo("#item-info");
        jQuery(".user-owns-" + d.key)
            .addClass("user-item-clicked");
    } else if (jQuery(".user-clicked").length !== 0) {
        svg.selectAll(".node-user-clicked")
            .classed("node-user-clicked", false);
        svg.selectAll(".link-user-clicked")
            .classed("link-user-clicked", false);
        jQuery(".user-clicked")
            .removeClass("user-clicked");
        
        jQuery(".user-clicked")
            .removeClass("user-clicked");
        svg.selectAll("path.link.target-" + d.key)
            .classed("link-item-clicked", true);
        svg.selectAll("path.link.source-" + d.key)
            .classed("link-item-clicked", true);
        svg.select("#node-" + d.key)
            .classed("item-clicked", true);
        jQuery(createItemInfo(d))
            .appendTo("#item-info");
        jQuery(".user-owns-" + d.key)
            .addClass("user-item-clicked");
    } else if (svg.select("#node-" + d.key).classed("item-clicked")) {
        svg.selectAll(".node-item-clicked")
            .classed("node-item-clicked", false);
        svg.selectAll(".link-item-clicked")
            .classed("link-item-clicked", false);
        svg.selectAll(".item-clicked")
            .classed("item-clicked", false);
        jQuery(".user-item-clicked")
            .removeClass("user-item-clicked");
        jQuery(".item-info")
            .remove();
    } else {
        svg.selectAll(".node-item-clicked")
            .classed("node-item-clicked", false);
        svg.selectAll(".link-item-clicked")
            .classed("link-item-clicked", false);
        svg.selectAll(".item-clicked")
            .classed("item-clicked", false);
        jQuery(".user-item-clicked")
            .removeClass("user-item-clicked");
        jQuery(".item-info")
            .remove();
        
        svg.selectAll("path.link.target-" + d.key)
            .classed("link-item-clicked", true);
        svg.selectAll("path.link.source-" + d.key)
            .classed("link-item-clicked", true);
        svg.select("#node-" + d.key)
            .classed("item-clicked", true);
        jQuery(createItemInfo(d))
            .appendTo("#item-info");
        jQuery(".user-owns-" + d.key)
            .addClass("user-item-clicked");
    }
}

function clickUser(user) {
    if (svg.selectAll(".item-clicked").empty() && jQuery(".user-clicked").length === 0) {
        svg.selectAll(".link-owner-" + user)
            .classed("link-user-clicked", true);
        svg.selectAll(".node-owner-" + user)
            .classed("node-user-clicked", true);
        jQuery("#user-" + user)
            .addClass("user-clicked");
    } else if (! svg.selectAll(".item-clicked").empty()) {
        svg.selectAll(".node-item-clicked")
            .classed("node-item-clicked", false);
        svg.selectAll(".link-item-clicked")
            .classed("link-item-clicked", false);
        svg.selectAll(".item-clicked")
            .classed("item-clicked", false);
        jQuery(".user-item-clicked")
            .removeClass("user-item-clicked");
        jQuery(".item-info")
            .remove();
        
        svg.selectAll(".link-owner-" + user)
            .classed("link-user-clicked", true);
        svg.selectAll(".node-owner-" + user)
            .classed("node-user-clicked", true);
        jQuery("#user-" + user)
            .addClass("user-clicked");
    } else if (jQuery("#user-" + user).hasClass("user-clicked")) {
        svg.selectAll(".link-user-clicked")
            .classed("link-user-clicked", false);
        svg.selectAll(".node-user-clicked")
            .classed("node-user-clicked", false);
        jQuery(".user-clicked")
            .removeClass("user-clicked");
    } else {
        svg.selectAll(".link-user-clicked")
            .classed("link-user-clicked", false);
        svg.selectAll(".node-user-clicked")
            .classed("node-user-clicked", false);
        jQuery(".user-clicked")
            .removeClass("user-clicked");
        
        svg.selectAll(".link-owner-" + user)
            .classed("link-user-clicked", true);
        svg.selectAll(".node-owner-" + user)
            .classed("node-user-clicked", true);
        jQuery("#user-" + user)
            .addClass("user-clicked");
    }
}

function createItemInfo(d) {
    var info
            = "<div id=\"#item-" + d.key + "\" class=\"item-info\">"
            + "<p>" + d.description + "</p>"
            + ((d.recommendation)?"<button onclick=\"addRecommendation('" + d.key + "');\">Add to library</button>":"")
            ;
    return info + "</div>"
}