import { useEffect, useState } from "react";
import TitleBar from './TitleBar'
import Positions from './Positions'
import Options from './Options'
import 'bootstrap/dist/css/bootstrap.min.css';
import './App.css';

function App() {
	const [accounts, setAccounts] = useState([]);
	const [selectedAccount, setSelectedAccount] = useState(null);
	const [portfolio, setPortfolio] = useState(null);

	useEffect(() => {
		const requestAccounts = async () => {
			const response = await fetch("http://localhost:8090/accounts");
			return await response.json();
		};
		requestAccounts().then((accounts) => {
			setAccounts(accounts);
			if (accounts.length > 0) {
				setSelectedAccount(accounts[0]);
			}
		});
	}, []);

	useEffect(() => {
		const requestPortfolio = async () => {
			const response = await fetch(`http://localhost:8090/portfolio?accountName=${selectedAccount}`);
			return await response.json();
		};
		if (selectedAccount) {
			requestPortfolio().then(setPortfolio);
		}
	}, [selectedAccount]);

	return (
		<div>
			<TitleBar accounts={accounts} setSelectedAccount={setSelectedAccount} />
			{
				portfolio ? (
					<div className="pm-row">
						<div className="pm-column">
							<Positions portfolio={portfolio} />
						</div>
						<div className="pm-column">
							<Options portfolio={portfolio} isLong={false} />
							<Options portfolio={portfolio} isLong={true} />
						</div>
					</div>
				) : (
					<div>No data available for "{selectedAccount}".</div>
				)
			}
		</div>
	);
}

export default App;
