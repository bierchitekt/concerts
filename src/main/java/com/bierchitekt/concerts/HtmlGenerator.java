package com.bierchitekt.concerts;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.util.Locale.ENGLISH;

@Slf4j
@Service
public class HtmlGenerator {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE dd LLLL yyyy").localizedBy(ENGLISH);

    public void generateHtml(List<ConcertDTO> concertDTOS) {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        try {
            objectMapper.writeValue(new File("concerts.json"), concertDTOS);
        } catch (IOException e) {
            log.error("error while writing concerts to json", e);
            throw new RuntimeException(e);
        }

        String tdOpenTag = "<td>";
        String tdCloseTag = "</td>";
        StringBuilder result = new StringBuilder("""
                  <!DOCTYPE html>
                  <html lang="de">
                  <head>
                  <meta http-equiv="Content-Type"
                        content="text/html; charset=utf-8">
                      <title>All Concerts in munich</title>
                       <style>
                        body {
                          font-family: Arial, sans-serif;
                          margin: 0;
                          padding: 0;
                          background-color: #121212;
                          color: #e0e0e0;
                        }
                        .collapsible {
                          background-color: #000;
                          color: #ff4081;
                          cursor: pointer;
                          padding: 10px;
                          width: 100%;
                          border: none;
                          text-align: left;
                          outline: none;
                          font-size: 18px;
                        }
                        .active, .collapsible:hover {
                          background-color: #444;
                        }
                        .content {
                          padding: 0 15px;
                          display: block;
                          overflow: hidden;
                          background-color: #1e1e1e;
                        }
                        table {
                          width: 95%;
                          margin: 20px auto;
                          border-collapse: collapse;
                          background-color: #1e1e1e;
                          box-shadow: 0 4px 8px rgba(0, 0, 0, 0.7);
                          table-layout: fixed;  /* Ermöglicht flexiblere Darstellung der Spalten */
                        }
                        th, td {
                          padding: 12px;
                          text-align: left;
                          border-bottom: 1px solid #444;
                          white-space: normal;  /* Normaler Textumbruch */
                          overflow-wrap: break-word; /* Umbruch innerhalb der Zellen */
                        }
                        th {
                          background-color: #000;
                          color: #ff4081;
                        }
                        tr:hover {
                          background-color: #333;
                        }
                        a {
                          color: #82b1ff;
                          text-decoration: none;
                        }
                        p {
                          text-align: center;
                        }
                        a:hover {
                          text-decoration: underline;
                        }
                      </style>
                
                        <script>
                          document.addEventListener("DOMContentLoaded", function() {
                            var coll = document.getElementsByClassName("collapsible");
                            for (var i = 0; i < coll.length; i++) {
                              coll[i].addEventListener("click", function() {
                                this.classList.toggle("active");
                                var content = this.nextElementSibling;
                                if (content.style.display === "block") {
                                  content.style.display = "none";
                                } else {
                                  content.style.display = "block";
                                }
                              });
                            }
                          });
                        </script>
                  </head>
                <div>
                    <p><a href="https://t.me/MunichMetalConcerts">join the telegram METAL channel to get the newest updates</a></p>
                    <p><a href="https://t.me/MunichRockConcerts">join the telegram ROCK channel to get the newest updates</a></p>
                    <p><a href="https://t.me/MunichPunkConcerts">join the telegram PUNK channel to get the newest updates</a></p>
                </div>
                <div>
                    <p>Filter for Metal: <input type="checkbox" id="filterMetalCheckBox"></p>
                    <p>Filter for Rock:  <input type="checkbox" id="filterRockCheckBox"></p>
                    <p>Filter for Punk:  <input type="checkbox" id="filterPunkCheckBox"></p>
                    <p><button onclick="filterMetal()">Apply Filter</button>
                </div>
                
                  <script>
                
                      function filterMetal() {
                
                          var checkBox = document.getElementById("filterMetalCheckBox");
                
                          const filterList = new Array();
                
                          if (document.getElementById("filterMetalCheckBox").checked == true) {
                              filterList.push("METAL")
                          }
                          if (document.getElementById("filterRockCheckBox").checked == true) {
                              filterList.push("ROCK")
                          }
                          if (document.getElementById("filterPunkCheckBox").checked == true) {
                              filterList.push("PUNK")
                          }
                
                          console.log(filterList);
                          var input, table, tr, td, i, txtValue;
                          input = document.getElementById("myInput");
                          table = document.getElementById("concertTable");
                          tr = table.getElementsByTagName("tr");
                
                
                          for (i = 0; i < tr.length; i++) {
                              tr[i].style.display = "";
                          }
                
                          if (filterList.length != 0) {
                              // Loop through all table rows, and hide those who don't match the search query
                              for (i = 0; i < tr.length; i++) {
                                  td = tr[i].getElementsByTagName("td")[2];
                                  if (td) {
                                      txtValue = td.textContent || td.innerText;
                
                                      if (txtValue.toUpperCase().indexOf(filterList[0]) > -1 || txtValue.toUpperCase().indexOf(filterList[1]) > -1 || txtValue.toUpperCase().indexOf(filterList[2]) > -1) {
                                          tr[i].style.display = "";
                                      } else {
                                          tr[i].style.display = "none";
                                      }
                
                
                
                                  }
                              }
                          }
                      }
                
                      </script>
                
                   <table id="concertTable">
                     <tr>
                        <th>Band/Date</th>
                        <th>Genre</th>
                        <th>Support</th>
                        <th width=120>Location</th>
                     </tr>
                """);
        LocalDate lastDate = LocalDate.MIN;
        for (ConcertDTO concertDTO : concertDTOS) {
            if (lastDate.isBefore(concertDTO.date())) {
                result.append("<tr><td><b>").append(concertDTO.date().format(formatter)).append("</b><td><td><td></tr>");
                lastDate = concertDTO.date();
            }

            result.append("<tr>\n");
            String title = concertDTO.title();


            result.append(tdOpenTag).append("<a target=\\”_blank\\” href=\"").append(concertDTO.link()).append("\">").append(title).append("</a>").append(tdCloseTag);
            String genre = String.join(", ", concertDTO.genre());

            result.append(tdOpenTag).append(genre).append(tdCloseTag);
            result.append(tdOpenTag).append(concertDTO.supportBands()).append(tdCloseTag);
            result.append(tdOpenTag).append(concertDTO.location()).append(tdCloseTag);
            result.append("</tr>");
        }
        result.append("</table></html>");
        try (PrintWriter out = new PrintWriter("result.html")) {
            out.println(result);
        } catch (Exception ex) {
            log.warn("exception while generating html", ex);

        }
    }
}
