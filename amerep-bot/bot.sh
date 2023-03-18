session=amerep-bot-amethy-terminal

start() {
  tmux has-session -t $session || tmux new-session -d -s $session
  tmux send-keys -t $session C-c
  tmux send-keys -t $session "bash" C-m
  tmux send-keys -t $session "cd $PWD" C-m
  tmux send-keys -t $session "npm run start" C-m
}

stop() {
  tmux send-keys -t $session C-c
  tmux kill-session -t $session
}

attach() {
  tmux attach -t $session
}

if [ "$1" == "start" ]; then
  start
elif [ "$1" == "stop" ]; then
  stop
elif [ "$1" == "restart" ]; then
  stop
  start
elif [ "$1" == "attach" ]; then
  attach
else
  echo Unknown arguments.
fi