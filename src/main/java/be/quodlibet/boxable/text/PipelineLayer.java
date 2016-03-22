package be.quodlibet.boxable.text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.font.PDFont;

/**
 * 
 * @author Markus Kühne
 *
 */

public class PipelineLayer {
	
	private final Pattern whitespace = Pattern.compile("(?:\r\n|\n\r|[ \t\n\r\\f])+\\z");
	
	private final StringBuilder text = new StringBuilder();
	
	private String lastTextToken = "";
	
	private List<Token> tokens = new ArrayList<>();
	
	private String trimmedLastTextToken = "";
	
	private float width;
	
	private float widthLastToken;
	
	private float widthTrimmedLastToken;
	
	private float widthCurrentText;
	
	public void push(final Token token) {
		tokens.add(token);
	}
	
	public void push(final PDFont font, final float fontSize, final Token token) throws IOException {
		text.append(lastTextToken);
		width += widthLastToken;
		lastTextToken = token.getData();
		trimmedLastTextToken = whitespace.matcher(lastTextToken).replaceAll("");
		widthLastToken = (font.getStringWidth(lastTextToken) / 1000f * fontSize);
		widthTrimmedLastToken = (font.getStringWidth(trimmedLastTextToken) / 1000f * fontSize);
		widthCurrentText = (font.getStringWidth(text.toString()) / 1000f * fontSize);
		
		push(token);
	}
	
	public void push(final PipelineLayer pipeline) {
		text.append(lastTextToken);
		width += widthLastToken;
		text.append(pipeline.text);
		if(pipeline.text.length()>0){
			width += pipeline.widthCurrentText;
		}
		lastTextToken = pipeline.lastTextToken;
		trimmedLastTextToken = pipeline.trimmedLastTextToken;
		widthLastToken = pipeline.widthLastToken;
		widthTrimmedLastToken = pipeline.widthTrimmedLastToken;
		tokens.addAll(pipeline.tokens);
		
		pipeline.reset();
	}
	
	public void reset() {
		text.delete(0, text.length());
		width = 0.0f;
		lastTextToken = "";
		trimmedLastTextToken = "";
		widthLastToken = 0.0f;
		widthTrimmedLastToken = 0.0f;
		tokens.clear();
	}
	
	public String trimmedText() {
		return text.toString() + trimmedLastTextToken;
	}
	
	public float width() {
		return width + widthLastToken;
	}
	
	public float trimmedWidth() {
		return width + widthTrimmedLastToken;
	}
	
	public List<Token> tokens() {
		return new ArrayList<>(tokens);
	}
	
	@Override
	public String toString() {
		return text.toString() + "(" + lastTextToken + ") [width: " + width() + ", trimmed: " + trimmedWidth() + "]";
	}
}
