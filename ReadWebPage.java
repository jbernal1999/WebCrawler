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
   //public static String htmlContent;
   
   private static final String DELIMITER = "\t";
   private static final String SEPARATOR = "\n";
    
    //File header
    private static final String HEADER = "Title\tPath\tAuthor\tServings\tIngredients\tProcedure";
       
   public static void main(String[] args) throws IOException, InterruptedException {
      ArrayList<ArrayList<String>> recipes = new ArrayList<ArrayList<String>>();
      ArrayList<String> pageUrls = new ArrayList<String>();

      pageUrls.add("https://www.surlatable.com/recipes/?srule=best-matches&start=0&sz=24");
      pageUrls.add("https://www.surlatable.com/recipes/?srule=best-matches&start=24&sz=24");
      pageUrls.add("https://www.surlatable.com/recipes/?srule=best-matches&start=48&sz=24");
      pageUrls.add("https://www.surlatable.com/recipes/?srule=best-matches&start=72&sz=24");
      
      
      int totalRecipes = 24;
      int recipeNumber=0;
        
      for(int a=0; a<96; a++)
      {
         ArrayList<String> filler = new ArrayList<String>(7);
         recipes.add(filler);
      }
      
      final ReadWebPage scraper = new ReadWebPage();
        
        for(int i=0; i<pageUrls.size(); i++)
        {
          String inputPageUrl = pageUrls.get(i);
          String htmlContent = scraper.getContent(inputPageUrl);
          //System.out.println(htmlContent);
          
          for(; recipeNumber<totalRecipes; recipeNumber++)
          {
            if(recipeNumber==24 || recipeNumber== 48 || recipeNumber==72)
               startx=0; 
            //get first arraylist and then add recipe url to the filler arraylist 
            recipes.get(recipeNumber).add(scraper.extractRecipeUrl(htmlContent));
            //System.out.println(scraper.extractRecipeUrl(htmlContent));
            final String recipeContent = scraper.getRecipeContent(recipes.get(recipeNumber).get(0));
            recipes.get(recipeNumber).add(scraper.extractTitle(recipeContent));
            recipes.get(recipeNumber).add(scraper.extractPath(recipeContent, 0));
            recipes.get(recipeNumber).add(scraper.extractAuthor(recipeContent));
            recipes.get(recipeNumber).add(scraper.extractServings(recipeContent));
            recipes.get(recipeNumber).add(scraper.extractIngredients(recipeContent));
            recipes.get(recipeNumber).add(scraper.extractProcedure(recipeContent));

              
            
           // Thread.sleep(10000);
          }
         totalRecipes+=24;
          
          //System.out.println(pageUrls.get(i));
        }
         //System.out.println(recipeNumber);
         System.out.println(recipes.get(25).get(0));
         
      FileWriter file = null;
      
      try{
        file = new FileWriter("RecipeList.csv");
        //Add header
        file.append(HEADER);
        //Add a new line after the header
        file.append(SEPARATOR);
        String toAdd = "";
        for(int a=0; a<96; a++)
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
            toAdd = toAdd.replaceAll("&amp;","&");
            toAdd = toAdd.replaceAll("&rsquo;","'");
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
      
    
    private String extractTitle(String content)
    {
      //retrieve Title 
      final Pattern authorRegExp = Pattern.compile("<title>(.*?)</title>", Pattern.DOTALL);
      final Matcher matcher = authorRegExp.matcher(content);
      try{
         matcher.find();
         return matcher.group(1);
      }catch(IllegalStateException ISE){
         return "";
      }
    }
    
       //extract the author for each recipe
   private String extractAuthor(String content) 
   {
      //retrieve author 
      final Pattern authorRegExp = Pattern.compile("<div class=\"recipe-author\">\n(.*?)\n</div>", Pattern.DOTALL);
      final Matcher matcher = authorRegExp.matcher(content);
      try{
         matcher.find();
  
         return matcher.group(1);
      }catch(IllegalStateException ISE){
         return "";
      }
    }
    //extract the ingredients for each recipe 
    private String extractIngredients(String content) 
    {
      String removeLines; 
      final Pattern servingsRegExp = Pattern.compile("<div class=\"recipe-details-ingredients\">(.*?)\n</div>", Pattern.DOTALL);
      final Matcher matcher = servingsRegExp.matcher(content);
      try{  
         matcher.find();
         removeLines = matcher.group(1);
         removeLines = removeLines.replaceAll("\n", " ");
         return removeLines;
      }catch(IllegalStateException ISE){
         return "";
      }
    }
    
    //retreive Procedure
    private String extractProcedure(String content)
    {
      String removeLines2; 
      final Pattern servingsRegExp = Pattern.compile("<div class=\"recipe-details-procedure\">\n(.*?)\n</div>", Pattern.DOTALL);
      final Matcher matcher = servingsRegExp.matcher(content);
      
      try{
         matcher.find();
         removeLines2 = matcher.group(1);
         removeLines2 = removeLines2.replaceAll("\n", " ");
         return removeLines2;
      }catch(IllegalStateException ISE){
         return "";
      }
    }
    
    //retreieve Serving Size
    private String extractServings(String content)
    {
      final Pattern servingsRegExp = Pattern.compile("<div class=\"recipe-details-serves\">\n(.*?)\n", Pattern.DOTALL);
      final Matcher matcher = servingsRegExp.matcher(content);
      try{
         matcher.find();
  
         return matcher.group(1);
      }catch(IllegalStateException ISE){
         return "";
      }
    }
    
    private String extractPath(String content, int startz)
    {
      try{
         Pattern pathRegExp = Pattern.compile("<a class=\"breadcrumb-element\" href=\".*?\" title=\".*?\">(.*?)</a>", Pattern.DOTALL);
         String group1 = "";
         String group2 = "";
         String path = "";
         Matcher matcher = pathRegExp.matcher(content);
         
         for(int i=0; i<4; i++)
         {
            matcher.find(startz);
            group1 += matcher.group(1) + "/";
            startz = matcher.start()+2;
         }
         path += group1;
         
         pathRegExp = Pattern.compile("<span class=\"breadcrumb-element\">(.*?)</span>", Pattern.DOTALL);
         matcher = pathRegExp.matcher(content);
         
         matcher.find();
         group2 = matcher.group(1);
         
   
         path += group2 ;     
         return path;
      }catch(IllegalStateException ISE){
         return "";
      }
    }
   
      
      
    
}
