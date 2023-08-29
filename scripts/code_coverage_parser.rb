require 'json'
require 'nokogiri'

begin
    html_report = File.read('chartboostcore/build/reports/kover/html/index.html')
    parsed_html = Nokogiri::HTML(html_report)

    # Line coverage is the 4th span with class 'percent'
    percentage = parsed_html.css('span.percent')[3].content.strip.to_f.round
rescue StandardError => e
    puts "Failed to parse HTML report: #{e.message}. Defaulting to 0%."
    percentage = 0
end

json_data = {
  'schemaVersion' => 1,
  'label' => 'Code Coverage',
  'message' => '0%',
  'color' => 'limegreen'
}

json_data['message'] = "#{percentage}%"
output_file_path = 'coverage-percent.json'

begin
    File.write(output_file_path, JSON.pretty_generate(json_data))
rescue StandardError => e
    puts "Failed to write to #{output_file_path}: #{e.message}"
    exit(1)
end
