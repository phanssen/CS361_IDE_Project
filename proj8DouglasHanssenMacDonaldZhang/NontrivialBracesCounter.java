/*
* Name: NontrivialBracesCounter.java
* Authors: Kyle Douglas, Paige Hanssen, Wyett McDonald, Tia Zhang
* CS361 Project 8
* Date: November 7, 2018
*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.FileNotFoundException;

/**
 * A class that finds the number of non-trivial left braces in a file.
 * A 'non-trivial' brace is any brace that would cause the Java program
 * to not compile if it was replaced with another character.
 * @author Kyle Douglas, Paige Hanssen, Wyett MacDonald, Tia Zhang
 */
public class NontrivialBracesCounter { 

	private BufferedReader reader;
	private boolean commentOpen; 
	private boolean stringOpen;

	/**
	* Prepares a stream with a file's text inside. Does not process the stream itself
	* @param filename is the file to become a stream
	*/
	public void readFileStream(String filename) throws IOException{
		try{File file = new File(filename);
			InputStream in = new FileInputStream(file);
			Reader inputStreamReader = new InputStreamReader(in);
			reader = new BufferedReader(inputStreamReader);
		}
		catch(FileNotFoundException e){
			System.out.println("File was not found. Please check your spelling");
			System.exit(0);
		}
	}
	
	/**
	* Counts the number of nontrivial braces in a legal Java file
	* A non-trivial brace is a brace that can't be replaced with another char without the program failing to function
	* @param filename is the legal Java program to be read
	* @return the number of nontrivial braces in the program
	*/
	public int getNumNontrivialLeftBraces (String filename) throws IOException {
		readFileStream(filename);
		int c;
		commentOpen = false;
		stringOpen = false;
		int nonTrivialBraces = 0;

		while((c = reader.read()) != -1) {
			char character = (char) c;
			System.out.println(character);  
			//If it's a left brace not inside an open comment or open string, add to counter
			if ((character == '{') && (commentOpen == false) && (stringOpen == false)){
				nonTrivialBraces++;
			}
			//if it's a single quote, ignore the next two chars cause it'll be a char and end quote. 
			//I don't care if it's inside a comment or string
			//Handled differently from " becuse not using the stringOpen boolean for both avoids
			//nested quotes issues like '\"'
			else if(character == '\'' && !stringOpen && !commentOpen){
				reader.read();
				reader.read();
			}
			// If the character is a backslash, ignore the next character because the
			// backslash serves as an escape character
			else if(character == '\\') {
				reader.read();
			}
			//If it's a double quote and not in a comment, then it's part of a string. 
			//If the string was open, it's now closed. If no string active, new one was opened
			else if ((character == '\"') && (commentOpen == false)){
				stringOpen = !stringOpen;
			}
			//If it's a /, it's either //, /*, or it's inside a string
			else if (((character == '/') && (!stringOpen))){
				handleForwardSlash();
			}
			//If it's */ and not inside a string, then the most recent comment has been closed. 
			else if ((character == '*') && (!stringOpen)){
				handleAsterisk();
			}
  		}
  		return nonTrivialBraces;
  	}	

  	/**
  	* To be called if the current char in the stream in the stream is a /
  	* Handles appropriate trivial braces ignoring based on whether the slash began 
  	* the beginning of a single line or multi line comment
  	*/
  	private void handleForwardSlash() throws IOException{
		char nextnextchar =  (char) reader.read(); //Named nextnextchar since the current one is usually nextChar, nextInt, etc
		//If it began a multline comment, then set the boolean for tracking open comments to true 
		if (nextnextchar == '*'){
			commentOpen = true;
		}
		else if ((nextnextchar == '/') && (!commentOpen)){
			//If single line comment that's not in a multiline (or string), eat the rest of the line
			reader.readLine();				
		}
	}

  	private void handleAsterisk() throws IOException{
  		char nextnextchar =  (char) reader.read();
		//Nested if to avoid eating the char if first is not a *. 
		//There shouldn't be a legal way to have an *{, so I don't care if the next char is consumed
		if(nextnextchar == '/'){
			commentOpen = false;
		}
  	}	

	public static void main (String[] args) throws IOException {
		if(args.length < 1){
			System.out.println("Usage: java NontrivialBracesCounter [Legal Java File's Name]");
			System.exit(0);
		}
		String filename = args[0];
		NontrivialBracesCounter nonTrivialBraces = new NontrivialBracesCounter();
		System.out.println(nonTrivialBraces.getNumNontrivialLeftBraces(filename));
	}
}	