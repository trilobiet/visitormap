package com.trilobiet.visitorstats.ingester;

public record CSVIngestSettings(
	
	Separator separator,
	int skiplines,
	int batchSize

) {
	
	public CSVIngestSettings() {
		this(Separator.COMMA, 0, 10_000);
	}
	
	public CSVIngestSettings(Separator sep) {
		this(sep, 0, 10_000);
	}

	public CSVIngestSettings(Separator sep, int skipLines) {
		this(sep, skipLines, 10_000);
	}

	public enum Separator {
		
		SPACE(' '),
		COMMA(','),
		COLON(':'),
		SEMICOLON(';'),
		TAB('\t'),
		PIPE('|');
		
		public final char character;
		
		private Separator(char c) {
			this.character = c;
		}
	}
	
}
