import sys
import os
def main():
	if len(sys.argv) < 2:
		sys.exit(0)
	os.system(" ".join(sys.argv[1:]))
if __name__ == "__main__":
	main()
