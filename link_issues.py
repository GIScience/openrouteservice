import re
import os

issue_match = re.compile('\([a-zA-Z\s]*#[0-9]+[a-zA-Z\s]*\)')
issue_number_match = re.compile('[0-9]+')

with open('CHANGELOG.md', 'r') as cl:
    with open('CHANGELOG2.md', 'w') as cl_w:
        for line in cl:
            result = issue_match.findall(line)
            if result:
                for n in result:
                    issue = n[1:-1]
                    issue_number = issue_number_match.findall(issue)[0]
                    link = 'https://github.com/GIScience/openrouteservice/issues/{}'.format(issue_number)
                    line = line.replace(issue, '[{}]({})'.format(issue, link))
            cl_w.write(line)

os.remove('CHANGELOG.md')
os.rename('CHANGELOG2.md', 'CHANGELOG.md')
