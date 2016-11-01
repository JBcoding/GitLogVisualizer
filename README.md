# GitLogVisualizer

Create a cool looking video of the progress of a git-project, with 2 simple steps.
1. Generate a log fil called "log.txt", with the "--name-status" tag. Ex: "git log --name-status > log.txt"
2. Generate the video with this command: "java -cp out/production/GitLogVisualizer/ Driver | ffmpeg -y -r 30 -f image2pipe -vcodec ppm -i - -vcodec libx264 -preset ultrafast -pix_fmt yuv420p -crf 1 -threads 4 -bf 0 movie.mp4"
