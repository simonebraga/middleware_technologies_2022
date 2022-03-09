output_dir=umlDiagrams
png_files=$(addprefix $(output_dir)/, $(addsuffix .png, $(basename $(wildcard *.uml))))

all: $(png_files)

$(output_dir)/%.png: %.uml
	plantuml -o $(output_dir) $^
