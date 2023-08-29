require 'find'

puts "Description: This script inserts a copyright notice at the top of all eligible files in the Core repo."

file_extensions_accepted = %w[.java .kt .xml .gradle]
exclusions = %w[chartboostcore/build/ build/ .idea/ /build/intermediates/ BuildConfig /build/.transforms/]
path_to_chartboost_core = ARGV.find { |arg| arg =~ /--path=.*/ }&.gsub('--path=', '')

puts "💬 PATH TO CHARTBOOST CORE: #{path_to_chartboost_core}"

unless File.directory?(path_to_chartboost_core)
    abort "❌ PATH #{path_to_chartboost_core} IS NOT A DIRECTORY. EXITING..."
end

files = Find.find(path_to_chartboost_core).reject { |path| File.directory?(path) || !file_extensions_accepted.any? { |ext| path.end_with?(ext) } }
files.reject! { |file| exclusions.any? { |exclusion| file.include?(exclusion) } }

if files.empty?
    abort "❌ NO ELIGIBLE FILES FOUND FOR COPYRIGHT NOTICE INSERTION. EXITING..."
end

puts "\n*** #{files.length} FILES FOUND THAT WILL BE PROCESSED FOR COPYRIGHT INSERTION ***"
puts files

current_year = Time.now.year
copyright_year_range = current_year > 2023 ? "2023-#{current_year}" : "#{current_year}"

files.each_with_index do |file, index|
    puts "\n(#{index + 1}/#{files.length}) Processing file: #{file}"

    content = File.read(file)
    comment_start, comment_middle, comment_end = case File.extname(file)
                                                 when '.java', '.kt', '.gradle'
                                                     ['/*', ' *', ' */']
                                                 when '.xml'
                                                     ['<!--', ' ', '-->']
                                                 else
                                                     next
                                                 end

    copyright_notice = "Copyright #{copyright_year_range} Chartboost, Inc.\n\nUse of this source code is governed by an MIT-style\nlicense that can be found in the LICENSE file."
    notice_lines = copyright_notice.split("\n")
    notice_as_comment = comment_start + "\n" + notice_lines.map { |line| comment_middle + " " + line }.join("\n") + "\n" + comment_end
    notice_as_comment += File.extname(file) == '.xml' ? "\n" : "\n\n"

    if content.include?("Copyright #{copyright_year_range}")
        puts "File already has current copyright notice. Skipping..."
        next
    elsif content =~ /Copyright 2023-20\d\d/
        puts "Year is not current. Updating year..."
        content.gsub!(/Copyright 2023-20\d\d/, "Copyright 2023-#{current_year}")
    else
        puts "File does not have copyright notice. Inserting..."
        xml_declaration = content[/.*<\?xml.*/]
        if xml_declaration
            notice_as_comment = xml_declaration + "\n" + notice_as_comment
        end
    end

    File.write(file, content.sub(xml_declaration || "", notice_as_comment))
end
