import { Manager } from 'alarm-manager';

window.testEcho = () => {
    const inputValue = document.getElementById("echoInput").value;
    Manager.echo({ value: inputValue })
}
