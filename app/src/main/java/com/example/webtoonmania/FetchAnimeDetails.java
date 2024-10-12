package com.example.webtoonmania;

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FetchAnimeDetails extends AsyncTask<String, Void, List<AnimeModel>> {
    private RecAdapter animeAdapter;

    public FetchAnimeDetails(RecAdapter animeAdapter) {
        this.animeAdapter = animeAdapter;
    }

    @Override
    protected List<AnimeModel> doInBackground(String... urls) {
        List<AnimeModel> animeList = new ArrayList<>();
        int count=0;
        try {
            // Connect to the website
            Document doc = Jsoup.connect(urls[0]).get();
            // Fetch all titles, images, and descriptions

            // Fetch all <h3> elements
            Elements headings = doc.select("h3"); // Select all <h3> elements
            for (int i = 0; i < headings.size(); i++) {
                if(count >=11){// Stop fetching after 10 items
                    break;
                }
                Element heading = headings.get(i); // Get the current heading
                //H3 tags full text
                String title = heading.text();

                // Fetch the image from the following <figure> tag
                String imageUrl = null;
                if (i == 0) { // For the first <h3>
                    Element firstImg = heading.nextElementSibling(); // Get the next sibling element
                    if (firstImg != null && firstImg.tagName().equals("div")) {
                        // The next element is a <div>, so look for the next <p> inside that <div>
                        Element pTag = firstImg.nextElementSibling(); // This should be the <p> tag containing the <img>
                        if (pTag != null) {
                            Element img = pTag.select("img").first();
                            if (img != null) {
                                imageUrl = img.hasAttr("data-src") ? img.attr("data-src") : img.attr("src");
                            }
                        }
                    }
                } else { // For all other <h3> elements
                    Element figure = heading.nextElementSibling(); // Get the next sibling element
                    if (figure != null && figure.tagName().equals("figure")) {
                        Element img = figure.select("img").first();
                        if (img != null) {
                            imageUrl = img.hasAttr("data-src") ? img.attr("data-src") : img.attr("src");
                        }
                    }
                }
                // Check if the image URL is null and handle it
                if (imageUrl == null || imageUrl.startsWith("data:image")) {
                    Log.d("AnimeDetails", "No valid image found for title: " + title);
                    // You can provide a default image URL or set it to a specific value
                    imageUrl = "https://upload.wikimedia.org/wikipedia/commons/d/d1/Image_not_available.png"; // Replace with a valid default image URL
                }

              // String description = null; // Initialize description variable


                // Fetch the description from the <p> tag before the <ul>
                // Fetch the description from the <p> tag before the <ul>
                String description = "No description available"; // Default description
                Element nextElement = heading.nextElementSibling();
                boolean firstParagraphSkipped = false;  // Flag to indicate if the first <p> has been skipped
                boolean secondParagraphFetched = false; // Flag to check if the second <p> has been fetched
                boolean checkedAfterDiv = false;        // Flag to ensure we only check after <div> once

                while (nextElement != null) {
                    // Check if current element is a <p> tag
                    if (nextElement.tagName().equals("p")) {
                        String paragraphText = nextElement.text();

                        // Skip the first <p> and any that start with "Also Read"
                        if (!firstParagraphSkipped && !paragraphText.startsWith("Also Read")) {
                            firstParagraphSkipped = true; // Skip the first valid <p>
                        }
                        // Fetch the second valid <p> and set it as description
                        else if (firstParagraphSkipped && !secondParagraphFetched && !paragraphText.startsWith("Also Read")) {
                            description = paragraphText;  // Fetch the second valid <p>
                            Log.d("AnimeDetails", "2nd Des: " + description);
                            secondParagraphFetched = true; // Mark the second <p> as fetched

                            // Add the item to the animeList after fetching the second <p>
                            animeList.add(new AnimeModel(title, imageUrl, description));
                            Log.d("AnimeDetails", "Item added: Title: " + title + " | Image URL: " + imageUrl + " | Description: " + description);
                            count++;  // Increment count after adding the item

                            // Reset title and image for the next item
                            title = "";
                            imageUrl = "";
                        }
                    }

                    // After the second <p> is fetched, check for <div class="code-block code-block-9"> and a <p> tag after it
                    if (secondParagraphFetched && !checkedAfterDiv && nextElement.tagName().equals("div") && nextElement.hasClass("code-block") && nextElement.hasClass("code-block-9")) {
                        Element nextAfterDiv = nextElement.nextElementSibling();
                        Log.e("AnimeDetails", "Description before <p>: " + description);

                        // Check if there's a <p> tag after this specific <div>
                        if (nextAfterDiv != null && nextAfterDiv.tagName().equals("p")) {
                            String paragraphText = nextAfterDiv.text();
                            description = paragraphText; // Fetch the <p> after the <div>
                            Log.e("AnimeDetails", "Description after <div class='code-block code-block-9'>: " + description);

                            // Add the third <p> tag too
                            animeList.add(new AnimeModel(title, imageUrl, description));
                            count++; // Increment count after adding the item

                            // Reset title and image for the next item
                            title = "";
                            imageUrl = "";
                        }
                        checkedAfterDiv = true; // Ensure we only check for <p> after <div> once
                    }

                    // Break the loop after fetching the third <p> (to prevent duplicate processing)
                    if (secondParagraphFetched && checkedAfterDiv) {
                        break;
                    }

                    nextElement = nextElement.nextElementSibling(); // Move to the next sibling
                }


                // Add logging to ensure we're not missing any descriptions
                Log.d("AnimeDetails", "Title: " + title + "| Final Description: " + description);

            }

        } catch (IOException e) {
            Log.e("AnimeDetails", "Error fetching details", e);
        }
        return animeList;
    }

    @Override
    protected void onPostExecute(List<AnimeModel> animeList) {
        // Update your RecyclerView here
        if (animeAdapter != null) {
            animeAdapter.updateData(animeList); // This should be an instance method call
        }
    }
}

