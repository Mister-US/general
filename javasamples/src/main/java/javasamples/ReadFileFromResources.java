package javasamples;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class ReadFileFromResources {

	public static void main(String[] args) {
		
		String input = "";
		InputStream istream = ReadFileFromResources.class.getClassLoader().getResourceAsStream("TestFile");
		
		input = new BufferedReader(new InputStreamReader(istream)).lines().collect(Collectors.joining("\n"));
		
		System.out.println(input);

	}

}
