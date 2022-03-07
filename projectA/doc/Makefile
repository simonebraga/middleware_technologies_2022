png_files=$(addsuffix .png, $(basename $(wildcard *.uml)))
output_dir="umlDiagrams"

all: $(png_files)

%.png: %.uml
	plantuml -o $(output_dir) $^
