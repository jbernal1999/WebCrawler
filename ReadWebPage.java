import java.io.IOException;
import java.util.Scanner;
import java.net.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.util.*;
import java.lang.String;
import java.lang.Thread;

public class ReadWebPage 
{
   public static int startx = 0;
   public static String inputPageUrl;
   public static String htmlContent;
   
   private static final String DELIMITER = ",";
   private static final String SEPARATOR = "\n";
    
    //File header
    private static final String HEADER = "Title,Path,Author,Servings,Ingredients,Procedure";
        
   public static void main(String[] args) throws IOException, InterruptedException {
      ArrayList<ArrayList<String>> recipes = new ArrayList<ArrayList<String>>();
      ArrayList<String> pageUrls = new ArrayList<String>();

      pageUrls.add("https://www.surlatable.com/recipes/?srule=best-matches&start=0&sz=24");
      pageUrls.add("https://www.surlatable.com/recipes/?srule=best-matches&start=24&sz=24");
      pageUrls.add("https://www.surlatable.com/recipes/?srule=best-matches&start=48&sz=24");
      pageUrls.add("https://www.surlatable.com/recipes/?srule=best-matches&start=72&sz=24");
      
      
      int size1 = 3;
      int j=0;
        
      for(int a=0; a<size1; a++)
      {
         ArrayList<String> filler = new ArrayList<String>(7);
         recipes.add(filler);
      }
      
      final ReadWebPage scraper = new ReadWebPage();
        
        for(int i=0; i<pageUrls.size(); i++)
        {
          inputPageUrl = pageUrls.get(i);
          htmlContent = scraper.getContent(inputPageUrl);
          
          for(; j<size1; j++)
          {
            //get first arraylist and then add recipe url to the filler arraylist 
            recipes.get(j).add(scraper.extractRecipeUrl(htmlContent));
            final String recipeContent = scraper.getRecipeContent(recipes.get(j).get(0));
            recipes.get(j).add(scraper.extractTitle(recipeContent));
            recipes.get(j).add(scraper.extractPath(recipeContent));
            recipes.get(j).add(scraper.extractAuthor(recipeContent));
            recipes.get(j).add(scraper.extractServings(recipeContent));
            recipes.get(j).add(scraper.extractIngredients(recipeContent));
            recipes.get(j).add(scraper.extractProcedure(recipeContent));

            if(j==23 || j== 47 || j==71)
               startx=0;
            
            Thread.sleep(10000);
          }
        }
        
      //System.out.println(recipes.get(1).get(0));
      /*System.out.println(recipes.get(0).get(2));
      System.out.println(recipes.get(0).get(3));
      System.out.println(recipes.get(0).get(4));
      System.out.println(recipes.get(0).get(5));*/

      //System.out.println(recipes.get(2).get(0));
      //System.out.println(recipes.get(23).get(0));
      //System.out.println(recipes.get(0).get(1));
      
      FileWriter file = null;
      
      try{
        file = new FileWriter("RecipeList.csv");
        //Add header
        file.append(HEADER);
        //Add a new line after the header
        file.append(SEPARATOR);
        String toAdd = "";
        for(int a=0; a<size1; a++)
        {
         for(int b=1; b<7; b++){
            toAdd = recipes.get(a).get(b);
            
            toAdd = toAdd.replaceAll("<li>","");
            toAdd = toAdd.replaceAll("</li>","");
            toAdd = toAdd.replaceAll("<br>","");
            toAdd = toAdd.replaceAll("<b>","");
            toAdd = toAdd.replaceAll("</b>","");
            toAdd = toAdd.replaceAll("</ul>","");
            toAdd = toAdd.replaceAll("<ul>","");
            toAdd = toAdd.replaceAll("<i>","");
            toAdd = toAdd.replaceAll("</i>","");
            toAdd = toAdd.replaceAll("&#188;","¼");
            toAdd = toAdd.replaceAll("&#189;","½");
            toAdd = toAdd.replaceAll("&#8539;","⅛");
            toAdd = toAdd.replaceAll("&#176;","°");
            file.append(toAdd);
            file.append(DELIMITER);

         } 
            file.append(SEPARATOR);            
        
        }
        file.close();
   
      }catch(Exception e)
      {
        e.printStackTrace();
      }
   }
        
   
   private String getContent(String url) throws IOException 
   { 
      try
      {
         HttpClient client = HttpClient.newHttpClient();
         String urlToScrape = url;
         HttpRequest request =  HttpRequest.newBuilder().uri(URI.create(urlToScrape)).build();
         HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());
         return response.body();
      }
      catch (InterruptedException e)
      {
         System.out.println(e);
      }
      return null;   
   }

   private String extractRecipeUrl(String content) 
   {
      final Pattern recipeUrlRegExp = Pattern.compile("<a class=\"thumb-link\" href=\"(.*?)\" title", Pattern.DOTALL);
      final Matcher matcher = recipeUrlRegExp.matcher(content);
      matcher.find(startx);
      startx = matcher.start()+20;
      
      return matcher.group(1);
   }
   
   private String getRecipeContent(String recipe) throws IOException
   {
      try
      {
        HttpClient client = HttpClient.newHttpClient();
        String urlToScrape = recipe;
        HttpRequest request =  HttpRequest.newBuilder().uri(URI.create(urlToScrape)).build();
        HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());
        return response.body();      
      }
      catch (InterruptedException e)
      {
        System.out.println(e);
      }
      return null;   
   }
      
   //extract the author for each recipe
   private String extractAuthor(String content) 
   {
      //retrieve author 
      final Pattern authorRegExp = Pattern.compile("<div class=\"recipe-author\">\n(.*?)\n</div>", Pattern.DOTALL);
      final Matcher matcher = authorRegExp.matcher(content);
      matcher.find();
      return matcher.group(1);
    }
    
    private String extractTitle(String content)
    {
      //retrieve Title 
      final Pattern authorRegExp = Pattern.compile("<title>(.*?)</title>", Pattern.DOTALL);
      final Matcher matcher = authorRegExp.matcher(content);
      matcher.find();
      return matcher.group(1);
    }
    //extract the ingredients for each recipe 
    private String extractIngredients(String content) 
    {
      final Pattern servingsRegExp = Pattern.compile("<div class=\"recipe-details-ingredients\">(.*?)\n</div>", Pattern.DOTALL);
      final Matcher matcher = servingsRegExp.matcher(content);
      matcher.find();
      return matcher.group(1);
    }
    
    //retreive Procedure
    private String extractProcedure(String content)
    {
      final Pattern servingsRegExp = Pattern.compile("<div class=\"recipe-details-procedure\">\n(.*?)\n</div>", Pattern.DOTALL);
      final Matcher matcher = servingsRegExp.matcher(content);
      matcher.find();
      return matcher.group(1);
    }
    
    //retreieve Serving Size
    private String extractServings(String content)
    {
      final Pattern servingsRegExp = Pattern.compile("<div class=\"recipe-details-serves\">\n(.*?)\n</div>", Pattern.DOTALL);
      final Matcher matcher = servingsRegExp.matcher(content);
      matcher.find();
      return matcher.group(1);
    }
    
    private String extractPath(String content)
    {
      //final Pattern servingsRegExp = Pattern.compile("title=\"Go to(.*?)\">", Pattern.DOTALL);
      //final Matcher matcher = servingsRegExp.matcher(content);
      //matcher.find();
      String path = "Home/Recipes/" ;
      //servingsRegExp = Pattern.compile("\"primaryCategory\":\"(.*?)\"", Pattern.DOTALL);
   
      return path;
    }
   
      
      
    
}
