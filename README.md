# syncFile2EXIF
Syncronizes file attributes, for pics, with corresponding information from EXIF data.
 `````
 Usage: java -jar <jarFileName> [command] [parameters]
		 [command] -> help
		 [command] -> times java -jar <jarFileName> times <directory> <fileNamePattern>
			 ex.: java -jar <jarFileName> times . .jpg
		 [command] -> name java -jar <jarFileName> name <directory> <fileNamePattern>
			 ex.: java -jar <jarFileName> name . .jpg
		 [command] -> setTimes java -jar <jarFileName> setTimes <directory> <fileNamePattern> <yyyyMMddHHmmss>
			 ex.: java -jar <jarFileName> setTimes . .jpg 20181225120000
 ````` 
Please, feel free to contact me for any question, including bugs ;) or feature request.
