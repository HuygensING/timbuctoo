module Timer
    @@begin_time = ""

    def Timer.dot nr
	if (nr % 1000) == 0
	    if (nr % 10000) == 0
		STDERR.write "#{nr/10000}"
	    elsif (nr % 5000) == 0
		STDERR.write "+"
	    else
		STDERR.write "."
	    end
	end

    end

    def Timer.start
	@@begin_time = Time.new
	puts "start at: #{@@begin_time}"
	STDERR.puts "start at: #{@@begin_time}"
    end

    def Timer.stop
	end_time = Time.new
	time_lapse = end_time - @@begin_time
	STDERR.puts
	puts
	if time_lapse < 100
	    puts "end at: #{end_time}, after #{sprintf("%2.1f",time_lapse)} seconds"
	    STDERR.puts "end at: #{end_time}, after #{sprintf("%2.1f",time_lapse)} seconds"
	else
	    min_sec = time_lapse.divmod(60)
	    minutes = min_sec[0]
	    seconds = min_sec[1]
	    puts "end at: #{end_time}, after #{sprintf("%2.0f",minutes)} minutes, #{sprintf("%2.1f",seconds)} seconds"
	    STDERR.puts "end at: #{end_time}, after #{sprintf("%2.0f",minutes)} minutes, #{sprintf("%2.1f",seconds)} seconds"
	end
    end
end

