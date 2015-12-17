Regaring TheMovieDb api key:

 Please put key for testing into API_KEY_HERE part of app/build.gradle file.

     buildTypes.each {
         it.buildConfigField 'String', 'THEMOVDB_API_KEY', '\"API_KEY_HERE\"'
     }