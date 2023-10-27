import Button from '@mui/material/Button';

const Test = () => {

    const Teststyle = {
        backgroundColor:'red'
    }


    return ( 
        <div style={Teststyle}>
           <Button variant="outlined">Outlined</Button>
           <Button variant="outlined">Outlined</Button>
        </div>

    )
}

export default Test;