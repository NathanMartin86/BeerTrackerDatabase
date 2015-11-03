package com.theironyard;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;


public class Main {
    static void insertBeer(Connection conn, String name, String type) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO beers VALUES (?,?)");
        stmt.setString(1, name);
        stmt.setString(2, type);
        stmt.execute();
    }

    static void deleteBeer(Connection conn, int idNum) throws SQLException {
        PreparedStatement stmt2 = conn.prepareStatement("DELETE FROM beers WHERE ROWNUM = ?");
        stmt2.setInt(1, idNum);
        stmt2.execute();
    }

    static void editBeer(Connection conn, String name, int idNum, String type) throws SQLException {
        PreparedStatement stmt3 = conn.prepareStatement("UPDATE beers SET name = ?, SET type = ? WHERE ROWNUM = ?");
        stmt3.setString(1, name);
        stmt3.setString(2, type);
        stmt3.setInt(3, idNum);
        stmt3.execute();
    }
    static ArrayList<Beer> selectBeer (Connection conn) throws SQLException {
        Statement stmt4 = conn.createStatement();
        ResultSet results = stmt4.executeQuery("SELECT * FROM beers");
        ArrayList<Beer> beers = new ArrayList<>();
        int id = 1;
        while (results.next()) {
            String name = results.getString("name");
            String type = results.getString("type");
            Beer beer = new Beer(name,type);
            beer.id = id;
            id++;
            beers.add(beer);
        }
        return beers;
    }


    public static void main(String[] args) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS beers (name VARCHAR, type VARCHAR)");


        Spark.get(
                "/",
                ((request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");
                    if (username == null) {
                        return new ModelAndView(new HashMap(), "not-logged-in.html");
                    }
                    HashMap m = new HashMap();
                    m.put("username", username);
                    m.put("beers", selectBeer(conn));
                    return new ModelAndView(m, "logged-in.html");
                }),
                new MustacheTemplateEngine()
        );

        Spark.post(
                "/login",
                ((request, response) -> {
                    String username = request.queryParams("username");
                    Session session = request.session();
                    session.attribute("username", username);
                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/create-beer",
                ((request, response) -> {
                    //Beer beer = new Beer();
                    //beer.id = beers.size() + 1;
                    String name = request.queryParams("beername");
                    String type = request.queryParams("beertype");

                    insertBeer(conn, name, type);

                    response.redirect("/");

                    return "";
                })
        );

        Spark.post(
                "/edit-beer",
                (request, response) -> {
                    String id = request.queryParams("newBeerId");
                    int idNum = Integer.valueOf(id);
                    String name = request.queryParams("newBeerName");
                    String type = request.queryParams("newBeerType");

                    editBeer(conn, name, idNum, type);

                    response.redirect("/");

                    return "";

                }
        );

        Spark.post(
                "/delete-beer",
                ((request, response) -> {
                    String id = request.queryParams("beerid");
                    try {
                        int idNum = Integer.valueOf(id);
                        deleteBeer(conn, idNum);


//                       beers.remove(idNum - 1);
//                       for (int i = 0; i < beers.size(); i++) {
//                            beers.get(i).id = i + 1;
//                        }
                         } catch (Exception e) {
                    }

                        response.redirect("/");
                        return "";
                    })
                    );

    }

}

